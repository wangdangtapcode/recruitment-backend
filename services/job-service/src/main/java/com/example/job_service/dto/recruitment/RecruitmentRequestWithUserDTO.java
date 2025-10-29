package com.example.job_service.dto.recruitment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.job_service.model.RecruitmentRequest;
import com.example.job_service.utils.enums.RecruitmentRequestStatus;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class RecruitmentRequestWithUserDTO {
    private Long id;
    private String title;
    private Integer numberOfPositions;
    private String priorityLevel;
    private String reason;
    private String description;
    private String requirements;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    private String location;
    private boolean isExceedBudget;
    private RecruitmentRequestStatus status;
    private Long requesterId;
    private JsonNode requester;
    private Long approvedId;
    private JsonNode approver;
    private String approvalNotes;
    private LocalDateTime approvedAt;
    private Long departmentId;
    private JsonNode department;
    private String jobCategoryName;
    private boolean active;

    public static RecruitmentRequestWithUserDTO fromEntity(RecruitmentRequest entity) {
        RecruitmentRequestWithUserDTO dto = new RecruitmentRequestWithUserDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setNumberOfPositions(entity.getNumberOfPositions());
        dto.setPriorityLevel(entity.getPriorityLevel());
        dto.setReason(entity.getReason());
        dto.setDescription(entity.getDescription());
        dto.setRequirements(entity.getRequirements());

        // Chỉ set salary khi vượt quỹ
        if (entity.isExceedBudget()) {
            dto.setSalaryMin(entity.getSalaryMin());
            dto.setSalaryMax(entity.getSalaryMax());
            dto.setCurrency(entity.getCurrency());
        }

        dto.setLocation(entity.getLocation());
        dto.setExceedBudget(entity.isExceedBudget());
        dto.setStatus(entity.getStatus());
        dto.setRequesterId(entity.getRequesterId());
        dto.setApprovedId(entity.getApprovedId());
        dto.setApprovalNotes(entity.getApprovalNotes());
        dto.setApprovedAt(entity.getApprovedAt());
        dto.setDepartmentId(entity.getDepartmentId());
        dto.setJobCategoryName(entity.getJobCategory() != null ? entity.getJobCategory().getName() : null);
        dto.setActive(entity.isActive());
        return dto;
    }
}
