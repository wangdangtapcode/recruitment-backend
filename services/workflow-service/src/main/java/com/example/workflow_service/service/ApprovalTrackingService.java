package com.example.workflow_service.service;

import com.example.workflow_service.dto.PaginationDTO;
import com.example.workflow_service.dto.Response;
import com.example.workflow_service.dto.approval.ApprovalTrackingResponseDTO;
import com.example.workflow_service.dto.approval.ApproveStepDTO;
import com.example.workflow_service.dto.approval.CreateApprovalTrackingDTO;
import com.example.workflow_service.exception.CustomException;
import com.example.workflow_service.exception.IdInvalidException;
import com.example.workflow_service.model.ApprovalTracking;
import com.example.workflow_service.model.Workflow;
import com.example.workflow_service.model.WorkflowStep;
import com.example.workflow_service.repository.ApprovalTrackingRepository;
import com.example.workflow_service.repository.WorkflowRepository;
import com.example.workflow_service.repository.WorkflowStepRepository;
import com.example.workflow_service.utils.SecurityUtil;
import com.example.workflow_service.utils.enums.ApprovalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalTrackingService {

    private final ApprovalTrackingRepository approvalTrackingRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final RestTemplate restTemplate;

    @Value("${services.user-service.url:http://localhost:8081}")
    private String userServiceBaseUrl;

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
        return toResponseDTO(tracking);
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
            moveToNextStep(tracking);
        } else {
            // Reject
            tracking.setStatus(ApprovalStatus.REJECTED);
            tracking.setActionUserId(currentUserId);
            tracking.setActionAt(OffsetDateTime.now());
            tracking.setNotes(dto.getApprovalNotes());
            approvalTrackingRepository.save(tracking);
        }

        return toResponseDTO(tracking);
    }

    /**
     * Bước 6: Chuyển sang bước tiếp theo
     */
    private void moveToNextStep(ApprovalTracking currentTracking) {
        // Lấy step hiện tại
        WorkflowStep currentStep = workflowStepRepository.findById(currentTracking.getStepId())
                .orElseThrow(() -> new CustomException("Không tìm thấy workflow step"));

        // Lấy workflow từ step
        Workflow workflow = currentStep.getWorkflow();
        
        // Tìm bước tiếp theo
        WorkflowStep nextStep = workflowStepRepository
                .findByWorkflowIdAndStepOrder(workflow.getId(), currentStep.getStepOrder() + 1)
                .orElse(null);

        if (nextStep != null) {
            // Tìm người duyệt cho bước tiếp theo
            Long assignedUserId = findUserByPositionId(nextStep.getApproverPositionId());
            if (assignedUserId == null) {
                throw new CustomException(
                        "Không tìm thấy người dùng nào giữ vị trí position_id: " + nextStep.getApproverPositionId());
            }

            // Tạo ApprovalTracking cho bước tiếp theo
            ApprovalTracking nextTracking = new ApprovalTracking();
            nextTracking.setRequestId(currentTracking.getRequestId());
            nextTracking.setStepId(nextStep.getId());
            nextTracking.setStatus(ApprovalStatus.PENDING);
            nextTracking.setAssignedUserId(assignedUserId);
            nextTracking.setActionUserId(null);
            nextTracking.setActionAt(null);
            approvalTrackingRepository.save(nextTracking);
        } else {
            // Bước 7: Hoàn thành workflow
            // Cập nhật trạng thái của request (cần gọi service khác)
            // Ở đây chỉ log hoặc trigger event
        }
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
            ResponseEntity<Response<List<UserPositionDTO>>> response = 
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            requestEntity,
                            new ParameterizedTypeReference<Response<List<UserPositionDTO>>>() {}
                    );

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
        return toResponseDTO(tracking);
    }

    @Transactional(readOnly = true)
    public PaginationDTO getAll(
            Long requestId,
            ApprovalStatus status,
            Long assignedUserId,
            Pageable pageable
    ) {
        Page<ApprovalTracking> trackingPage = approvalTrackingRepository.findByFilters(
                requestId, status, assignedUserId, pageable
        );

        List<ApprovalTrackingResponseDTO> content = trackingPage.getContent().stream()
                .map(this::toResponseDTO)
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
        return trackings.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private ApprovalTrackingResponseDTO toResponseDTO(ApprovalTracking tracking) {
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
        return dto;
    }

    // DTO cho UserPosition từ user-service
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
}
