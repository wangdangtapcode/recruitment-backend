package com.example.job_service.dto.offer;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.job_service.model.Offer;
import com.example.job_service.utils.enums.OfferStatus;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class OfferWithUserDTO {
    private Long id;
    private Long candidateId;
    private JsonNode candidate; // Thông tin ứng viên từ candidate-service
    private Long positionId;
    private JsonNode position; // Thông tin vị trí từ job-service
    private LocalDate probationStartDate;
    private String notes;
    private OfferStatus status;
    private Long requesterId;
    private JsonNode requester;
    private Long ownerUserId;
    private Long workflowId;
    private JsonNode workflowInfo; // Thông tin workflow và approval tracking
    private Long departmentId;
    private JsonNode department;
    private Long currentStepId;
    private LocalDateTime submittedAt;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OfferWithUserDTO fromEntity(Offer entity) {
        OfferWithUserDTO dto = new OfferWithUserDTO();
        dto.setId(entity.getId());
        dto.setCandidateId(entity.getCandidateId());
        dto.setPositionId(entity.getPositionId());
        dto.setProbationStartDate(entity.getProbationStartDate());
        dto.setNotes(entity.getNotes());
        dto.setStatus(entity.getStatus());
        dto.setRequesterId(entity.getRequesterId());
        dto.setOwnerUserId(entity.getOwnerUserId());
        dto.setWorkflowId(entity.getWorkflowId());
        dto.setDepartmentId(entity.getDepartmentId());
        dto.setCurrentStepId(entity.getCurrentStepId());
        dto.setSubmittedAt(entity.getSubmittedAt());
        dto.setActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
