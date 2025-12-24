package com.example.job_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.job_service.dto.Meta;
import com.example.job_service.dto.PaginationDTO;
import com.example.job_service.dto.SingleResponseDTO;
import com.example.job_service.dto.offer.ApproveOfferDTO;
import com.example.job_service.dto.offer.CancelOfferDTO;
import com.example.job_service.dto.offer.CreateOfferDTO;
import com.example.job_service.dto.offer.OfferWithUserDTO;
import com.example.job_service.dto.offer.RejectOfferDTO;
import com.example.job_service.dto.offer.ReturnOfferDTO;
import com.example.job_service.dto.offer.UpdateOfferDTO;
import com.example.job_service.dto.offer.WithdrawOfferDTO;
import com.example.job_service.exception.IdInvalidException;
import com.example.job_service.exception.UserClientException;
import com.example.job_service.messaging.OfferWorkflowProducer;
import com.example.job_service.messaging.RecruitmentWorkflowEvent;
import com.example.job_service.model.Offer;
import com.example.job_service.repository.OfferRepository;
import com.example.job_service.utils.enums.OfferStatus;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class OfferService {
    private final OfferRepository offerRepository;
    private final UserClient userService;
    private final OfferWorkflowProducer workflowProducer;
    private final WorkflowClient workflowServiceClient;

    public OfferService(
            OfferRepository offerRepository,
            UserClient userService,
            OfferWorkflowProducer workflowProducer,
            WorkflowClient workflowServiceClient) {
        this.offerRepository = offerRepository;
        this.userService = userService;
        this.workflowProducer = workflowProducer;
        this.workflowServiceClient = workflowServiceClient;
    }

    @Transactional
    public Offer create(CreateOfferDTO dto) {
        Offer offer = new Offer();
        offer.setCandidateId(dto.getCandidateId());
        offer.setPositionId(dto.getPositionId());
        offer.setProbationStartDate(dto.getProbationStartDate());
        offer.setNotes(dto.getNotes());
        offer.setStatus(OfferStatus.DRAFT);
        offer.setDepartmentId(dto.getDepartmentId());
        offer.setIsActive(true);
        offer.setWorkflowId(dto.getWorkflowId());
        // requesterId sẽ được set từ controller

        return offerRepository.save(offer);
    }

    @Transactional
    public Offer update(Long id, UpdateOfferDTO dto) throws IdInvalidException {
        Offer offer = this.findById(id);
        if (offer.getStatus() != OfferStatus.DRAFT && offer.getStatus() != OfferStatus.RETURNED) {
            throw new IllegalStateException("Chỉ có thể cập nhật khi offer ở trạng thái DRAFT hoặc RETURNED");
        }

        if (dto.getCandidateId() != null) {
            offer.setCandidateId(dto.getCandidateId());
        }
        if (dto.getPositionId() != null) {
            offer.setPositionId(dto.getPositionId());
        }
        if (dto.getProbationStartDate() != null) {
            offer.setProbationStartDate(dto.getProbationStartDate());
        }
        if (dto.getNotes() != null) {
            offer.setNotes(dto.getNotes());
        }

        return offerRepository.save(offer);
    }

    @Transactional
    public Offer submit(Long id, Long actorId, String token) throws IdInvalidException {
        Offer offer = this.findById(id);
        if (offer.getStatus() != OfferStatus.DRAFT && offer.getStatus() != OfferStatus.RETURNED) {
            throw new IllegalStateException("Chỉ có thể submit khi offer ở trạng thái DRAFT hoặc RETURNED");
        }

        // Nếu chưa có workflowId, tự động tìm workflow type OFFER
        if (offer.getWorkflowId() == null && offer.getDepartmentId() != null) {
            Long workflowId = workflowServiceClient.findMatchingOfferWorkflow(offer.getDepartmentId());
            if (workflowId != null) {
                offer.setWorkflowId(workflowId);
            }
        }

        offer.setStatus(OfferStatus.PENDING);
        offer.setSubmittedAt(LocalDateTime.now());
        if (offer.getRequesterId() == null) {
            offer.setRequesterId(actorId);
        }
        if (offer.getOwnerUserId() == null) {
            offer.setOwnerUserId(actorId);
        }

        Offer saved = offerRepository.save(offer);
        publishWorkflowEvent("REQUEST_SUBMITTED", saved, actorId, null, null, null, null, token);
        return saved;
    }

    @Transactional
    public Offer approveStep(Long id, ApproveOfferDTO dto, Long actorId, String token) throws IdInvalidException {
        Offer offer = this.findById(id);
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể phê duyệt offer ở trạng thái PENDING");
        }

        Long currentStepId = offer.getCurrentStepId();
        Offer saved = offerRepository.save(offer);
        publishWorkflowEvent("REQUEST_APPROVED", saved, actorId, dto.getApprovalNotes(), null, currentStepId, null,
                token);
        return saved;
    }

    @Transactional
    public Offer rejectStep(Long id, RejectOfferDTO dto, Long actorId, String token) throws IdInvalidException {
        Offer offer = this.findById(id);
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể từ chối offer ở trạng thái PENDING");
        }

        Long currentStepId = offer.getCurrentStepId();
        offer.setStatus(OfferStatus.REJECTED);
        offer.setCurrentStepId(null);
        Offer saved = offerRepository.save(offer);
        publishWorkflowEvent("REQUEST_REJECTED", saved, actorId, null, dto.getReason(), currentStepId, null, token);
        return saved;
    }

    @Transactional
    public Offer returnOffer(Long id, ReturnOfferDTO dto, Long actorId, String token) throws IdInvalidException {
        Offer offer = this.findById(id);
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể trả về offer đang PENDING");
        }

        Long currentStepId = offer.getCurrentStepId();
        offer.setStatus(OfferStatus.RETURNED);
        Offer saved = offerRepository.save(offer);
        publishWorkflowEvent("REQUEST_RETURNED", saved, actorId, null, dto.getReason(), currentStepId,
                dto.getReturnedToStepId(), token);
        return saved;
    }

    @Transactional
    public Offer cancel(Long id, CancelOfferDTO dto, Long actorId, String token) throws IdInvalidException {
        Offer offer = this.findById(id);
        if (offer.getStatus() == OfferStatus.CANCELLED) {
            return offer;
        }

        if (offer.getStatus() == OfferStatus.APPROVED || offer.getStatus() == OfferStatus.REJECTED) {
            throw new IllegalStateException("Không thể hủy offer đã được APPROVED hoặc REJECTED");
        }

        Long currentStepId = offer.getCurrentStepId();
        offer.setStatus(OfferStatus.CANCELLED);
        offer.setCurrentStepId(null);
        Offer saved = offerRepository.save(offer);
        publishWorkflowEvent("REQUEST_CANCELLED", saved, actorId, null, dto.getReason(), currentStepId, null, token);
        return saved;
    }

    @Transactional
    public Offer withdraw(Long id, WithdrawOfferDTO dto, Long actorId, String token) throws IdInvalidException {
        Offer offer = this.findById(id);

        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể rút lại offer đang PENDING");
        }

        if (!offer.getOwnerUserId().equals(actorId) && !offer.getRequesterId().equals(actorId)) {
            throw new IllegalStateException("Chỉ submitter hoặc owner mới có thể rút lại offer");
        }

        Long currentStepId = offer.getCurrentStepId();
        offer.setStatus(OfferStatus.WITHDRAWN);
        offer.setCurrentStepId(null);
        Offer saved = offerRepository.save(offer);
        publishWorkflowEvent("REQUEST_WITHDRAWN", saved, actorId, null, dto.getReason(), currentStepId, null, token);
        return saved;
    }

    public Offer findById(Long id) throws IdInvalidException {
        return offerRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Offer không tồn tại"));
    }

    public OfferWithUserDTO getByIdWithUser(Long id, String token) throws IdInvalidException {
        Offer offer = this.findById(id);
        return convertToWithUserDTO(offer, token);
    }

    public SingleResponseDTO<OfferWithUserDTO> getByIdWithUserAndMetadata(Long id, String token)
            throws IdInvalidException {
        Offer offer = this.findById(id);
        OfferWithUserDTO dto = convertToWithUserDTO(offer, token);
        return new SingleResponseDTO<>(dto);
    }

    // public PaginationDTO getAllByDepartmentIdWithUser(Long departmentId, String token, Pageable pageable) {
    //     // Use getAllWithFilters which handles pagination properly
    //     return getAllWithFilters(departmentId, null, null, null, token, pageable);
    // }

    // public PaginationDTO getAllWithUser(String token, Pageable pageable) {
    //     Page<Offer> offers = offerRepository.findByIsActiveTrue(pageable);
    //     return convertToWithUserDTOList(offers, token);
    // }

    public PaginationDTO getAllWithFilters(Long departmentId, String status, Long createdBy, String keyword,
            String token, Pageable pageable) {
        OfferStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = OfferStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, will be ignored
            }
        }

        Page<Offer> offers = offerRepository.findByFilters(departmentId, statusEnum, createdBy, keyword, pageable);
        return convertToWithUserDTOList(offers, token);
    }

    @Transactional
    public boolean delete(Long id) throws IdInvalidException {
        Offer offer = this.findById(id);
        offer.setIsActive(false);
        offerRepository.save(offer);
        return true;
    }

    private OfferWithUserDTO convertToWithUserDTO(Offer offer, String token) {
        OfferWithUserDTO dto = OfferWithUserDTO.fromEntity(offer);

        if (offer.getRequesterId() != null) {
            ResponseEntity<JsonNode> requesterResponse = userService.getEmployeeById(offer.getRequesterId(), token);
            if (requesterResponse.getStatusCode().is2xxSuccessful()) {
                dto.setRequester(requesterResponse.getBody());
            } else {
                throw new UserClientException(requesterResponse);
            }
        }

        if (offer.getDepartmentId() != null) {
            ResponseEntity<JsonNode> departmentResponse = userService.getDepartmentById(offer.getDepartmentId(), token);
            if (departmentResponse.getStatusCode().is2xxSuccessful()) {
                dto.setDepartment(departmentResponse.getBody());
            } else {
                throw new UserClientException(departmentResponse);
            }
        }

        // Lấy thông tin candidate từ candidate-service
        if (offer.getCandidateId() != null) {
            // TODO: Gọi candidate-service để lấy thông tin candidate
            // Có thể tạo CandidateServiceClient tương tự WorkflowServiceClient
        }

        // Lấy thông tin position từ job-service (có thể lấy từ JobPositionRepository)
        if (offer.getPositionId() != null) {
            // TODO: Lấy từ JobPositionRepository hoặc gọi API
        }

        // Lấy thông tin workflow
        dto.setWorkflowId(offer.getWorkflowId());
        dto.setSubmittedAt(offer.getSubmittedAt());
        dto.setOwnerUserId(offer.getOwnerUserId());

        JsonNode workflowInfo = workflowServiceClient.getWorkflowInfoByRequestId(offer.getId(), offer.getWorkflowId(),
                token);
        if (workflowInfo != null) {
            dto.setWorkflowInfo(workflowInfo);
        }

        return dto;
    }

    private PaginationDTO convertToWithUserDTOList(Page<Offer> offers, String token) {
        PaginationDTO rs = new PaginationDTO();
        Meta mt = new Meta();
        mt.setPage(offers.getNumber() + 1);
        mt.setPageSize(offers.getSize());
        mt.setPages(offers.getTotalPages());
        mt.setTotal(offers.getTotalElements());
        rs.setMeta(mt);

        List<Long> employeeIds = offers.getContent().stream()
                .filter(o -> o.getRequesterId() != null)
                .map(Offer::getRequesterId)
                .distinct()
                .collect(Collectors.toList());

        List<Long> departmentIds = offers.getContent().stream()
                .map(Offer::getDepartmentId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, JsonNode> employeeMap = userService.getEmployeesByIds(employeeIds, token);

        Map<Long, JsonNode> departmentMap = departmentIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            try {
                                ResponseEntity<JsonNode> response = userService.getDepartmentById(id, token);
                                return response.getBody();
                            } catch (Exception e) {
                                return null;
                            }
                        }));

        rs.setResult(offers.getContent().stream()
                .map(offer -> {
                    OfferWithUserDTO dto = convertToWithUserDTO(offer, token);
                    if (offer.getRequesterId() != null) {
                        dto.setRequester(employeeMap.get(offer.getRequesterId()));
                    }
                    if (offer.getDepartmentId() != null) {
                        dto.setDepartment(departmentMap.get(offer.getDepartmentId()));
                    }
                    return dto;
                })
                .toList());
        return rs;
    }

    private void publishWorkflowEvent(String eventType, Offer offer, Long actorId, String notes, String reason,
            Long currentStepId, Long returnedToStepId, String authToken) {
        RecruitmentWorkflowEvent event = RecruitmentWorkflowEvent.builder()
                .eventType(eventType)
                .requestType("OFFER")
                .requestId(offer.getId())
                .workflowId(offer.getWorkflowId())
                .currentStepId(currentStepId != null ? currentStepId : offer.getCurrentStepId())
                .actorUserId(actorId)
                .notes(notes)
                .reason(reason)
                .requestStatus(offer.getStatus().name())
                .ownerUserId(offer.getOwnerUserId())
                .requesterId(offer.getRequesterId())
                .departmentId(offer.getDepartmentId())
                .occurredAt(LocalDateTime.now())
                .returnedToStepId(returnedToStepId)
                .build();
        event.setAuthToken(authToken);
        workflowProducer.publishEvent(event);
    }
}
