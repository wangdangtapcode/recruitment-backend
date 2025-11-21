package com.example.workflow_service.service;

import com.example.workflow_service.dto.PaginationDTO;
import com.example.workflow_service.dto.Response;
import com.example.workflow_service.dto.approval.ApprovalTrackingResponseDTO;
import com.example.workflow_service.dto.approval.ApproveStepDTO;
import com.example.workflow_service.dto.approval.CreateApprovalTrackingDTO;
import com.example.workflow_service.dto.approval.RequestWorkflowInfoDTO;
import com.example.workflow_service.dto.workflow.WorkflowResponseDTO;
import com.example.workflow_service.dto.workflow.WorkflowStepResponseDTO;
import com.example.workflow_service.exception.CustomException;
import com.example.workflow_service.exception.IdInvalidException;
import com.example.workflow_service.model.ApprovalTracking;
import com.example.workflow_service.model.Workflow;
import com.example.workflow_service.model.WorkflowStep;
import com.example.workflow_service.messaging.RecruitmentWorkflowEvent;
import com.example.workflow_service.messaging.NotificationProducer;
import com.example.workflow_service.messaging.RecruitmentWorkflowProducer;
import com.example.workflow_service.repository.ApprovalTrackingRepository;
import com.example.workflow_service.repository.WorkflowRepository;
import com.example.workflow_service.repository.WorkflowStepRepository;
import com.example.workflow_service.utils.SecurityUtil;
import com.example.workflow_service.utils.enums.ApprovalStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalTrackingService {

    private final ApprovalTrackingRepository approvalTrackingRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final RecruitmentWorkflowProducer workflowProducer;
    private final NotificationProducer notificationProducer;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(ApprovalTrackingService.class);

    private final RestTemplate restTemplate;

    @Value("${services.user-service.url:http://localhost:8081}")
    private String userServiceBaseUrl;

    @Value("${services.job-service.url:http://localhost:8083}")
    private String jobServiceBaseUrl;

    /**
     * Bước 1-4: Tạo yêu cầu và khởi tạo bước đầu tiên
     * Tự động tìm workflow phù hợp, tìm người duyệt và tạo ApprovalTracking
     */
    @Transactional
    public ApprovalTrackingResponseDTO initializeApproval(CreateApprovalTrackingDTO dto) {
        // Bước 2: Tìm workflow phù hợp
        Workflow workflow = workflowRepository.findMatchingWorkflow(
                dto.getDepartmentId(), dto.getLevelId())
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy workflow phù hợp với department_id: " + dto.getDepartmentId() +
                                " và level_id: " + dto.getLevelId()));

        // Bước 3: Lấy bước đầu tiên
        WorkflowStep firstStep = workflowStepRepository
                .findByWorkflowIdAndStepOrder(workflow.getId(), 1)
                .orElseThrow(() -> new CustomException(
                        "Workflow không có bước đầu tiên"));

        // Bước 4: Tìm người duyệt dựa trên position_id
        Long assignedUserId = findUserByPositionId(firstStep.getApproverPositionId());
        if (assignedUserId == null) {
            throw new CustomException(
                    "Không tìm thấy người dùng nào giữ vị trí position_id: " + firstStep.getApproverPositionId());
        }

        // Tạo ApprovalTracking
        ApprovalTracking tracking = new ApprovalTracking();
        tracking.setRequestId(dto.getRequestId());
        tracking.setStepId(firstStep.getId());
        tracking.setStatus(ApprovalStatus.PENDING);
        tracking.setAssignedUserId(assignedUserId);
        tracking.setActionUserId(null);
        tracking.setActionAt(null);

        tracking = approvalTrackingRepository.save(tracking);

        notifyNextApprovers(dto.getDepartmentId(), firstStep, dto.getRequestId(), null);

        // Lấy user name nếu có actionUserId
        Map<Long, String> userNamesMap = null;
        if (tracking.getActionUserId() != null) {
            String token = SecurityUtil.getCurrentUserJWT().orElse(null);
            userNamesMap = userService.getUserNamesByIds(
                    List.of(tracking.getActionUserId()),
                    token);
        }
        return toResponseDTO(tracking, null, userNamesMap);
    }

    /**
     * Bước 5: Người dùng phê duyệt/từ chối
     */
    @Transactional
    public ApprovalTrackingResponseDTO approve(Long id, ApproveStepDTO dto) {
        ApprovalTracking tracking = approvalTrackingRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy approval tracking với ID: " + id));

        // Kiểm tra quyền
        Long currentUserId = SecurityUtil.extractEmployeeId();
        if (!tracking.getAssignedUserId().equals(currentUserId)) {
            throw new CustomException("Bạn không có quyền phê duyệt bước này");
        }

        // Kiểm tra trạng thái
        if (tracking.getStatus() != ApprovalStatus.PENDING) {
            throw new CustomException("Bước này đã được xử lý");
        }

        if (dto.getApproved()) {
            // Approve
            tracking.setStatus(ApprovalStatus.APPROVED);
            tracking.setActionUserId(currentUserId);
            tracking.setActionAt(OffsetDateTime.now());
            tracking.setNotes(dto.getApprovalNotes());
            approvalTrackingRepository.save(tracking);

            // Bước 6: Chuyển sang bước tiếp theo
            moveToNextStep(tracking, null, null);
        } else {
            // Reject
            tracking.setStatus(ApprovalStatus.REJECTED);
            tracking.setActionUserId(currentUserId);
            tracking.setActionAt(OffsetDateTime.now());
            tracking.setNotes(dto.getApprovalNotes());
            approvalTrackingRepository.save(tracking);
        }

        // Lấy user name nếu có actionUserId
        Map<Long, String> userNamesMap = null;
        if (tracking.getActionUserId() != null) {
            String token = SecurityUtil.getCurrentUserJWT().orElse(null);
            userNamesMap = userService.getUserNamesByIds(
                    List.of(tracking.getActionUserId()),
                    token);
        }
        return toResponseDTO(tracking, null, userNamesMap);
    }

    /**
     * Tìm user_id dựa trên position_id (gọi RBAC service)
     */
    private Long findUserByPositionId(Long positionId) {
        try {
            String url = userServiceBaseUrl + "/api/v1/user-service/user-positions/by-position/" + positionId;

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            // Có thể thêm JWT token nếu cần

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<Response<List<UserPositionDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Response<List<UserPositionDTO>>>() {
                    });

            if (response.getBody() != null && response.getBody().getData() != null) {
                List<UserPositionDTO> userPositions = response.getBody().getData();
                if (!userPositions.isEmpty()) {
                    // Lấy user đầu tiên (có thể cần logic phức tạp hơn)
                    return userPositions.get(0).getUserId();
                }
            }
            return null;
        } catch (Exception e) {
            // Log error
            return null;
        }
    }

    @Transactional(readOnly = true)
    public ApprovalTrackingResponseDTO getById(Long id) {
        ApprovalTracking tracking = approvalTrackingRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy approval tracking với ID: " + id));

        // Lấy user name nếu có actionUserId
        Map<Long, String> userNamesMap = null;
        if (tracking.getActionUserId() != null) {
            String token = SecurityUtil.getCurrentUserJWT().orElse(null);
            userNamesMap = userService.getUserNamesByIds(
                    List.of(tracking.getActionUserId()),
                    token);
        }
        return toResponseDTO(tracking, null, userNamesMap);
    }

    @Transactional(readOnly = true)
    public PaginationDTO getAll(
            Long requestId,
            ApprovalStatus status,
            Long assignedUserId,
            Pageable pageable) {
        Page<ApprovalTracking> trackingPage = approvalTrackingRepository.findByFilters(
                requestId, status, assignedUserId, pageable);

        List<ApprovalTracking> trackings = trackingPage.getContent();

        // Thu thập tất cả user IDs từ trackings
        Set<Long> allUserIds = trackings.stream()
                .map(ApprovalTracking::getActionUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // Lấy token từ SecurityContext
        String token = SecurityUtil.getCurrentUserJWT().orElse(null);
        // Gọi user-service một lần để lấy tất cả user names
        Map<Long, String> userNamesMap = userService.getUserNamesByIds(
                allUserIds.stream().collect(Collectors.toList()),
                token);

        List<ApprovalTrackingResponseDTO> content = trackings.stream()
                .map(tracking -> toResponseDTO(tracking, null, userNamesMap))
                .collect(Collectors.toList());

        PaginationDTO paginationDTO = new PaginationDTO();
        com.example.workflow_service.dto.Meta meta = new com.example.workflow_service.dto.Meta();
        meta.setPage(trackingPage.getNumber() + 1);
        meta.setPageSize(trackingPage.getSize());
        meta.setPages(trackingPage.getTotalPages());
        meta.setTotal(trackingPage.getTotalElements());
        paginationDTO.setMeta(meta);
        paginationDTO.setResult(content);

        return paginationDTO;
    }

    @Transactional(readOnly = true)
    public List<ApprovalTrackingResponseDTO> getPendingApprovalsForUser(Long userId) {
        List<ApprovalTracking> trackings = approvalTrackingRepository
                .findByAssignedUserIdAndStatus(userId, ApprovalStatus.PENDING);

        // Thu thập tất cả user IDs từ trackings
        Set<Long> allUserIds = trackings.stream()
                .map(ApprovalTracking::getActionUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // Lấy token từ SecurityContext
        String token = SecurityUtil.getCurrentUserJWT().orElse(null);
        // Gọi user-service một lần để lấy tất cả user names
        Map<Long, String> userNamesMap = userService.getUserNamesByIds(
                allUserIds.stream().collect(Collectors.toList()),
                token);

        return trackings.stream()
                .map(tracking -> toResponseDTO(tracking, null, userNamesMap))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RequestWorkflowInfoDTO getWorkflowInfoByRequestId(
            Long requestId, Long workflowId) {
        // Lấy tất cả approval tracking của request này
        List<ApprovalTracking> trackings = approvalTrackingRepository.findByRequestId(requestId);

        // Tìm workflowId từ tracking hoặc từ parameter
        Long actualWorkflowId = workflowId;
        Long currentStepId = null;

        if (!trackings.isEmpty()) {
            ApprovalTracking firstTracking = trackings.get(0);
            WorkflowStep firstStep = workflowStepRepository.findById(firstTracking.getStepId())
                    .orElse(null);
            if (firstStep != null && actualWorkflowId == null) {
                actualWorkflowId = firstStep.getWorkflow().getId();
            }

            // Tìm bước hiện tại đang pending
            ApprovalTracking currentTracking = trackings.stream()
                    .filter(t -> t.getStatus() == ApprovalStatus.PENDING)
                    .findFirst()
                    .orElse(null);
            if (currentTracking != null) {
                currentStepId = currentTracking.getStepId();
            }
        }

        // Thu thập tất cả position IDs từ trackings và workflow steps
        Set<Long> allPositionIds = trackings.stream()
                .map(tracking -> {
                    WorkflowStep step = workflowStepRepository.findById(tracking.getStepId()).orElse(null);
                    return step != null ? step.getApproverPositionId() : null;
                })
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // Thu thập tất cả user IDs từ trackings
        Set<Long> allUserIds = trackings.stream()
                .map(ApprovalTracking::getActionUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // Lấy token từ SecurityContext
        String token = SecurityUtil.getCurrentUserJWT().orElse(null);
        // Gọi user-service một lần để lấy tất cả position names
        Map<Long, String> positionNamesMap = userService.getPositionNamesByIds(
                allPositionIds.stream().collect(Collectors.toList()),
                token);
        // Gọi user-service một lần để lấy tất cả user names
        Map<Long, String> userNamesMap = userService.getUserNamesByIds(
                allUserIds.stream().collect(Collectors.toList()),
                token);

        RequestWorkflowInfoDTO result = new RequestWorkflowInfoDTO();

        // Lấy thông tin workflow nếu có
        if (actualWorkflowId != null) {
            Workflow workflow = workflowRepository.findById(actualWorkflowId).orElse(null);
            if (workflow != null) {
                WorkflowResponseDTO workflowDTO = convertWorkflowToDTO(workflow, positionNamesMap);
                result.setWorkflow(workflowDTO);
            }
        }

        // Convert approval trackings với position names và user names
        List<ApprovalTrackingResponseDTO> trackingDTOs = trackings.stream()
                .map(tracking -> toResponseDTO(tracking, positionNamesMap, userNamesMap))
                .collect(Collectors.toList());
        result.setApprovalTrackings(trackingDTOs);
        result.setCurrentStepId(currentStepId);

        return result;
    }

    private com.example.workflow_service.dto.workflow.WorkflowResponseDTO convertWorkflowToDTO(Workflow workflow) {
        return convertWorkflowToDTO(workflow, null);
    }

    private com.example.workflow_service.dto.workflow.WorkflowResponseDTO convertWorkflowToDTO(Workflow workflow,
            Map<Long, String> positionNamesMap) {
        com.example.workflow_service.dto.workflow.WorkflowResponseDTO dto = new com.example.workflow_service.dto.workflow.WorkflowResponseDTO();
        dto.setId(workflow.getId());
        dto.setName(workflow.getName());
        dto.setDescription(workflow.getDescription());
        dto.setType(workflow.getType());
        dto.setApplyConditions(workflow.getApplyConditions());
        dto.setIsActive(workflow.getIsActive());
        dto.setCreatedBy(workflow.getCreatedBy());
        dto.setUpdatedBy(workflow.getUpdatedBy());
        dto.setCreatedAt(workflow.getCreatedAt());
        dto.setUpdatedAt(workflow.getUpdatedAt());

        // Convert steps nếu có
        if (workflow.getSteps() != null) {
            List<com.example.workflow_service.dto.workflow.WorkflowStepResponseDTO> stepDTOs = workflow.getSteps()
                    .stream()
                    .map(step -> convertStepToDTO(step, positionNamesMap))
                    .collect(Collectors.toList());
            dto.setSteps(stepDTOs);
        }

        return dto;
    }

    private WorkflowStepResponseDTO convertStepToDTO(WorkflowStep step) {
        return convertStepToDTO(step, null);
    }

    private WorkflowStepResponseDTO convertStepToDTO(WorkflowStep step, Map<Long, String> positionNamesMap) {
        WorkflowStepResponseDTO dto = new WorkflowStepResponseDTO();
        dto.setId(step.getId());
        dto.setStepOrder(step.getStepOrder());
        dto.setStepName(step.getStepName());
        dto.setApproverPositionId(step.getApproverPositionId());
        dto.setIsActive(step.getIsActive());
        dto.setCreatedAt(step.getCreatedAt());
        dto.setUpdatedAt(step.getUpdatedAt());

        // Set approverPositionName từ map nếu có
        if (positionNamesMap != null && step.getApproverPositionId() != null) {
            String positionName = positionNamesMap.get(step.getApproverPositionId());
            dto.setApproverPositionName(positionName);
        }

        return dto;
    }

    @Transactional
    public void handleWorkflowEvent(RecruitmentWorkflowEvent event) {
        if (event == null || event.getEventType() == null || event.getRequestId() == null) {
            return;
        }
        String eventType = event.getEventType().toUpperCase();
        switch (eventType) {
            case "REQUEST_SUBMITTED":
                handleRequestSubmitted(event);
                break;
            case "STEP_APPROVED":
                handleStepApproved(event);
                break;
            case "STEP_REJECTED":
                handleStepRejected(event);
                break;
            case "REQUEST_RETURNED":
                handleRequestReturned(event);
                break;
            case "REQUEST_CANCELLED":
                handleRequestCancelled(event);
                break;
            case "REQUEST_WITHDRAWN":
                handleRequestWithdrawn(event);
                break;
            // Backward compatibility
            case "REQUEST_APPROVED":
                handleStepApproved(event);
                break;
            case "REQUEST_REJECTED":
                handleStepRejected(event);
                break;
            default:
                log.debug("Ignore unknown workflow event type: {}", eventType);
        }
    }

    private ApprovalTrackingResponseDTO toResponseDTO(ApprovalTracking tracking) {
        return toResponseDTO(tracking, null, null);
    }

    private ApprovalTrackingResponseDTO toResponseDTO(ApprovalTracking tracking, Map<Long, String> positionNamesMap,
            Map<Long, String> userNamesMap) {
        ApprovalTrackingResponseDTO dto = new ApprovalTrackingResponseDTO();
        dto.setId(tracking.getId());
        dto.setRequestId(tracking.getRequestId());
        dto.setStepId(tracking.getStepId());
        dto.setStatus(tracking.getStatus());
        dto.setAssignedUserId(tracking.getAssignedUserId());
        dto.setActionUserId(tracking.getActionUserId());
        dto.setActionAt(tracking.getActionAt());
        dto.setNotes(tracking.getNotes());
        dto.setCreatedAt(tracking.getCreatedAt());
        dto.setUpdatedAt(tracking.getUpdatedAt());

        // Set actionUserName từ map nếu có
        if (userNamesMap != null && tracking.getActionUserId() != null) {
            String userName = userNamesMap.get(tracking.getActionUserId());
            dto.setActionUserName(userName);
        }

        return dto;
    }

    // DTO cho UserPosition từ user-service
    @SuppressWarnings("unused")
    private static class UserPositionDTO {
        private Long userId;
        private Long positionId;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getPositionId() {
            return positionId;
        }

        public void setPositionId(Long positionId) {
            this.positionId = positionId;
        }
    }

    private void moveToNextStep(ApprovalTracking currentTracking, Long departmentId, String authToken) {
        WorkflowStep currentStep = workflowStepRepository.findById(currentTracking.getStepId())
                .orElseThrow(() -> new CustomException("Không tìm thấy workflow step"));

        Workflow workflow = currentStep.getWorkflow();
        WorkflowStep nextStep = workflowStepRepository
                .findByWorkflowIdAndStepOrder(workflow.getId(), currentStep.getStepOrder() + 1)
                .orElse(null);

        if (nextStep != null) {
            Long effectiveDepartmentId = departmentId;
            createTrackingForStep(currentTracking.getRequestId(), nextStep, effectiveDepartmentId, authToken);
        }
    }

    private void handleRequestSubmitted(RecruitmentWorkflowEvent event) {
        if (event.getWorkflowId() == null) {
            log.warn("REQUEST_SUBMITTED missing workflowId for request {}", event.getRequestId());
            return;
        }

        // Kiểm tra xem có tracking nào đang pending không
        List<ApprovalTracking> pending = approvalTrackingRepository
                .findByRequestIdAndStatus(event.getRequestId(), ApprovalStatus.PENDING);

        if (!pending.isEmpty()) {
            boolean allReturnPlaceholders = pending.stream()
                    .allMatch(this::isReturnPlaceholderTracking);

            if (allReturnPlaceholders) {
                pending.forEach(tracking -> {
                    tracking.setStatus(ApprovalStatus.CANCELLED);
                    tracking.setActionType("RESUBMIT");
                    tracking.setActionUserId(event.getActorUserId());
                    tracking.setActionAt(OffsetDateTime.now());
                    tracking.setNotes("Placeholder cancelled due to resubmit");
                    approvalTrackingRepository.save(tracking);
                });
            } else {
                log.debug("Request {} already has pending approval step, skip initialization", event.getRequestId());
                return;
            }
        }

        // Kiểm tra xem có tracking nào đã bị return không (submit lại sau return)
        List<ApprovalTracking> allTrackings = approvalTrackingRepository.findByRequestId(event.getRequestId());
        ApprovalTracking returnedTracking = allTrackings.stream()
                .filter(t -> t.getReturnedToStepId() != null)
                .findFirst()
                .orElse(null);

        WorkflowStep targetStep;
        if (returnedTracking != null && returnedTracking.getReturnedToStepId() != null) {
            // Submit lại sau return: tạo tracking từ bước được trả về
            targetStep = workflowStepRepository.findById(returnedTracking.getReturnedToStepId())
                    .orElseThrow(() -> new CustomException(
                            "Không tìm thấy bước được trả về: " + returnedTracking.getReturnedToStepId()));
            log.info("Request {} submit lại từ bước {}", event.getRequestId(), targetStep.getId());
        } else {
            // Submit lần đầu: tạo tracking từ bước đầu tiên
            Workflow workflow = workflowRepository.findById(event.getWorkflowId())
                    .orElseThrow(() -> new CustomException("Workflow không tồn tại: " + event.getWorkflowId()));
            targetStep = workflowStepRepository
                    .findByWorkflowIdAndStepOrder(workflow.getId(), 1)
                    .orElseThrow(() -> new CustomException("Workflow không có bước đầu tiên"));
        }

        createTrackingForStep(event.getRequestId(), targetStep, event.getDepartmentId(), event.getAuthToken());
    }

    private void handleStepApproved(RecruitmentWorkflowEvent event) {
        ApprovalTracking current = markCurrentTracking(event, ApprovalStatus.APPROVED, "APPROVE", event.getNotes());
        if (current != null) {
            // Kiểm tra có bước tiếp theo không
            WorkflowStep currentStep = workflowStepRepository.findById(current.getStepId())
                    .orElseThrow(() -> new CustomException("Không tìm thấy workflow step: " + current.getStepId()));

            Workflow workflow = currentStep.getWorkflow();
            WorkflowStep nextStep = workflowStepRepository
                    .findByWorkflowIdAndStepOrder(workflow.getId(), currentStep.getStepOrder() + 1)
                    .orElse(null);

            if (nextStep != null) {
                // Có bước tiếp theo: chuyển sang bước tiếp
                moveToNextStep(current, event.getDepartmentId(), event.getAuthToken());
            } else {
                // Không còn bước nào: đã hoàn thành workflow
                // Kiểm tra xem còn tracking nào pending không
                List<ApprovalTracking> remainingPending = approvalTrackingRepository
                        .findByRequestIdAndStatus(event.getRequestId(), ApprovalStatus.PENDING);

                if (remainingPending.isEmpty()) {
                    // Không còn bước nào pending: workflow đã hoàn thành
                    log.info("Request {} đã hoàn thành tất cả các bước workflow", event.getRequestId());
                    // Gửi event WORKFLOW_COMPLETED để job-service cập nhật status = APPROVED
                    sendWorkflowCompletedEvent(event);
                }
            }
        }
    }

    private void handleStepRejected(RecruitmentWorkflowEvent event) {
        // Reject: Từ chối ở bước này, kết thúc luồng
        ApprovalTracking tracking = markCurrentTracking(event, ApprovalStatus.REJECTED, "REJECT", event.getNotes());
        if (tracking != null) {
            // Invalidate tất cả các bước tiếp theo (nếu có)
            invalidateFutureSteps(event.getRequestId(), tracking.getStepId());

            notifyRequester(event,
                    "Yêu cầu #" + event.getRequestId() + " bị từ chối",
                    event.getNotes() != null ? event.getNotes() : "Yêu cầu đã bị từ chối.");
        }
    }

    private void handleRequestReturned(RecruitmentWorkflowEvent event) {
        // Đánh dấu bước hiện tại là RETURNED
        ApprovalTracking currentTracking = findCurrentPendingTracking(event.getRequestId());
        if (currentTracking == null) {
            log.warn("Không tìm thấy approval tracking đang chờ cho request {}", event.getRequestId());
            return;
        }

        // Xác định bước cần trả về
        Long returnedToStepId = event.getReturnedToStepId();
        if (returnedToStepId == null) {
            // Nếu không chỉ định, trả về bước đầu tiên
            Workflow workflow = workflowRepository.findById(event.getWorkflowId())
                    .orElseThrow(() -> new CustomException("Workflow không tồn tại: " + event.getWorkflowId()));
            WorkflowStep firstStep = workflowStepRepository
                    .findByWorkflowIdAndStepOrder(workflow.getId(), 1)
                    .orElseThrow(() -> new CustomException("Workflow không có bước đầu tiên"));
            returnedToStepId = firstStep.getId();
        }

        // Đánh dấu bước hiện tại
        currentTracking.setStatus(ApprovalStatus.RETURNED);
        currentTracking.setActionType("RETURN");
        currentTracking.setActionUserId(event.getActorUserId());
        currentTracking.setActionAt(OffsetDateTime.now());
        currentTracking.setNotes(event.getReason());
        currentTracking.setReturnedToStepId(returnedToStepId);
        currentTracking.setReturnedAt(OffsetDateTime.now());
        approvalTrackingRepository.save(currentTracking);

        // Invalidate tất cả các bước đã qua (từ bước hiện tại trở về sau)
        invalidateFutureSteps(event.getRequestId(), currentTracking.getStepId());
        boolean isReturned = true;
        // Tạo tracking mới cho bước được trả về (để submitter/owner chỉnh sửa)
        createTrackingForReturnedStep(event, returnedToStepId, isReturned);

        notifyRequester(event,
                "Yêu cầu #" + event.getRequestId() + " bị trả về",
                event.getReason() != null ? event.getReason()
                        : "Yêu cầu cần chỉnh sửa lại thông tin.");

        log.info("Request {} được trả về bước {}", event.getRequestId(), returnedToStepId);
    }

    private void handleRequestCancelled(RecruitmentWorkflowEvent event) {
        // Cancel: Hủy yêu cầu (có thể ở bất kỳ trạng thái nào)
        // Invalidate tất cả các tracking đang pending
        List<ApprovalTracking> pendingTrackings = approvalTrackingRepository
                .findByRequestIdAndStatus(event.getRequestId(), ApprovalStatus.PENDING);

        for (ApprovalTracking tracking : pendingTrackings) {
            tracking.setStatus(ApprovalStatus.CANCELLED);
            tracking.setActionType("CANCEL");
            tracking.setActionUserId(event.getActorUserId());
            tracking.setActionAt(OffsetDateTime.now());
            tracking.setNotes(event.getReason());
            tracking.setCancelledByStepId(tracking.getStepId());
            tracking.setCancelledByUserId(event.getActorUserId());
            tracking.setCancelledAt(OffsetDateTime.now());
            approvalTrackingRepository.save(tracking);
        }

        notifyRequester(event,
                "Yêu cầu #" + event.getRequestId() + " đã bị hủy",
                event.getReason() != null ? event.getReason() : "Yêu cầu đã bị hủy.");

        log.info("Request {} đã bị hủy, đã invalidate {} tracking", event.getRequestId(), pendingTrackings.size());
    }

    private void handleRequestWithdrawn(RecruitmentWorkflowEvent event) {
        // Withdraw: Rút lại yêu cầu (chỉ submitter/owner mới có thể)
        // Invalidate tất cả các tracking đang pending
        List<ApprovalTracking> pendingTrackings = approvalTrackingRepository
                .findByRequestIdAndStatus(event.getRequestId(), ApprovalStatus.PENDING);

        for (ApprovalTracking tracking : pendingTrackings) {
            tracking.setStatus(ApprovalStatus.CANCELLED);
            tracking.setActionType("WITHDRAW");
            tracking.setActionUserId(event.getActorUserId());
            tracking.setActionAt(OffsetDateTime.now());
            tracking.setNotes(event.getReason());
            approvalTrackingRepository.save(tracking);
        }

        notifyRequester(event,
                "Yêu cầu #" + event.getRequestId() + " đã được rút lại",
                event.getReason() != null ? event.getReason() : "Yêu cầu đã được rút bởi người tạo.");

        log.info("Request {} đã bị rút lại, đã invalidate {} tracking", event.getRequestId(), pendingTrackings.size());
    }

    /**
     * Invalidate tất cả các bước đã qua (từ bước hiện tại trở về sau)
     * Dùng khi return hoặc reject
     */
    private void invalidateFutureSteps(Long requestId, Long currentStepId) {
        WorkflowStep currentStep = workflowStepRepository.findById(currentStepId)
                .orElseThrow(() -> new CustomException("Không tìm thấy workflow step: " + currentStepId));

        Workflow workflow = currentStep.getWorkflow();
        List<WorkflowStep> allSteps = workflowStepRepository.findByWorkflowIdOrderByStepOrderAsc(workflow.getId());

        // Tìm các bước có stepOrder >= currentStep.stepOrder
        List<Long> futureStepIds = allSteps.stream()
                .filter(step -> step.getStepOrder() > currentStep.getStepOrder())
                .map(WorkflowStep::getId)
                .collect(Collectors.toList());

        if (!futureStepIds.isEmpty()) {
            // Tìm và invalidate các tracking của các bước này
            List<ApprovalTracking> futureTrackings = approvalTrackingRepository.findByRequestId(requestId)
                    .stream()
                    .filter(t -> futureStepIds.contains(t.getStepId()) && t.getStatus() == ApprovalStatus.PENDING)
                    .collect(Collectors.toList());

            for (ApprovalTracking tracking : futureTrackings) {
                tracking.setStatus(ApprovalStatus.CANCELLED);
                tracking.setNotes("Automatically cancelled due to return/reject at step " + currentStepId);
                approvalTrackingRepository.save(tracking);
            }

            log.info("Đã invalidate {} tracking cho các bước tương lai của request {}", futureTrackings.size(),
                    requestId);
        }
    }

    /**
     * Khi request bị return, tạo một tracking mới cho bước được trả về để người
     * tạo/owner
     * có thể chỉnh sửa và submit lại.
     */
    private void createTrackingForReturnedStep(RecruitmentWorkflowEvent event, Long returnedToStepId,
            boolean isReturned) {
        WorkflowStep returnedStep = workflowStepRepository.findById(returnedToStepId)
                .orElseThrow(() -> new CustomException("Không tìm thấy bước được trả về: " + returnedToStepId));

        ApprovalTracking tracking = new ApprovalTracking();
        tracking.setRequestId(event.getRequestId());
        tracking.setStepId(returnedStep.getId());
        tracking.setCurrentStepId(returnedStep.getId());
        tracking.setStatus(ApprovalStatus.PENDING);

        // Ưu tiên assign cho requester/owner để họ chỉnh sửa, nếu không có thì fallback
        Long assignedUserId = event.getRequesterId() != null ? event.getRequesterId() : event.getOwnerUserId();
        if (assignedUserId == null) {
            assignedUserId = returnedStep.getApproverPositionId(); // fallback theo cấu hình bước
        }
        tracking.setAssignedUserId(assignedUserId);
        tracking.setNotes("Đang đợi cập nhật sau khi trả về");
        approvalTrackingRepository.save(tracking);
        if (!isReturned) {
            notifyNextApprovers(event.getDepartmentId(), returnedStep, event.getRequestId(), event.getAuthToken());
        }
    }

    private void notifyNextApprovers(Long departmentId, WorkflowStep step, Long requestId, String authToken) {
        if (step == null) {
            return;
        }
        Long effectiveDepartmentId = departmentId;
        if (effectiveDepartmentId == null) {
            log.warn("Không xác định được departmentId cho request {}", requestId);
            return;
        }

        String stepName = step.getStepName() != null ? step.getStepName()
                : "Bước " + step.getStepOrder();
        String title = "Yêu cầu #" + requestId + " cần xử lý";
        String message = "Bước '" + stepName + "' đang chờ phê duyệt.";

        notificationProducer.sendNotificationToDepartment(
                effectiveDepartmentId,
                step.getApproverPositionId(),
                title,
                message,
                authToken);
    }

    private void notifyRequester(RecruitmentWorkflowEvent event, String title, String message) {
        Long target = event.getRequesterId() != null ? event.getRequesterId() : event.getOwnerUserId();
        if (target == null) {
            return;
        }
        notificationProducer.sendNotification(target, title, message, event.getAuthToken());
    }

    private ApprovalTracking markCurrentTracking(RecruitmentWorkflowEvent event,
            ApprovalStatus status,
            String actionType,
            String notes) {
        ApprovalTracking tracking = findCurrentPendingTracking(event.getRequestId());
        if (tracking == null) {
            log.warn("Không tìm thấy approval tracking đang chờ cho request {}", event.getRequestId());
            return null;
        }
        tracking.setStatus(status);
        tracking.setActionType(actionType);
        tracking.setActionUserId(event.getActorUserId());
        tracking.setActionAt(OffsetDateTime.now());
        tracking.setNotes(notes);
        return approvalTrackingRepository.save(tracking);
    }

    private ApprovalTracking findCurrentPendingTracking(Long requestId) {
        return approvalTrackingRepository
                .findByRequestIdAndStatus(requestId, ApprovalStatus.PENDING)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private void createTrackingForStep(Long requestId, WorkflowStep step, Long departmentId, String authToken) {
        // Long assignedUserId = findUserByPositionId(step.getApproverPositionId());
        // if (assignedUserId == null) {
        // log.warn("Không tìm thấy user cho vị trí {} khi tạo bước mới cho request {}",
        // step.getApproverPositionId(),
        // requestId);
        // return;
        // }
        Long assignedUserId = step.getApproverPositionId();
        ApprovalTracking tracking = new ApprovalTracking();
        tracking.setRequestId(requestId);
        tracking.setStepId(step.getId());
        tracking.setCurrentStepId(step.getId());
        tracking.setStatus(ApprovalStatus.PENDING);
        tracking.setAssignedUserId(assignedUserId);
        approvalTrackingRepository.save(tracking);

        notifyNextApprovers(departmentId, step, requestId, authToken);
    }

    /**
     * Placeholder tracking được tạo khi request bị return để chờ submitter/owner
     * chỉnh sửa. Nhận diện bằng ghi chú mặc định.
     */
    private boolean isReturnPlaceholderTracking(ApprovalTracking tracking) {
        String notes = tracking.getNotes();
        return notes != null && notes.startsWith("Waiting for update after return");
    }

    /**
     * Gửi event WORKFLOW_COMPLETED khi workflow đã hoàn thành tất cả các bước
     */
    private void sendWorkflowCompletedEvent(RecruitmentWorkflowEvent originalEvent) {
        RecruitmentWorkflowEvent completedEvent = new RecruitmentWorkflowEvent();
        completedEvent.setEventType("WORKFLOW_COMPLETED");
        completedEvent.setRequestId(originalEvent.getRequestId());
        completedEvent.setWorkflowId(originalEvent.getWorkflowId());
        completedEvent.setActorUserId(originalEvent.getActorUserId());
        completedEvent.setNotes(originalEvent.getNotes());
        completedEvent.setRequestStatus("APPROVED");
        completedEvent.setOwnerUserId(originalEvent.getOwnerUserId());
        completedEvent.setRequesterId(originalEvent.getRequesterId());
        completedEvent.setDepartmentId(originalEvent.getDepartmentId());
        completedEvent.setOccurredAt(java.time.LocalDateTime.now());
        completedEvent.setAuthToken(originalEvent.getAuthToken());

        workflowProducer.publishEvent(completedEvent);
        log.info("Đã gửi event WORKFLOW_COMPLETED cho request {}", originalEvent.getRequestId());

        notifyRequester(originalEvent,
                "Yêu cầu #" + originalEvent.getRequestId() + " đã được phê duyệt",
                "Tất cả các bước phê duyệt đã hoàn thành.");
    }
}
