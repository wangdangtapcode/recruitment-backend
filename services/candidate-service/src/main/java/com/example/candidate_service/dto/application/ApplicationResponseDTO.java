package com.example.candidate_service.dto.application;

import java.time.LocalDate;

import com.example.candidate_service.utils.enums.ApplicationStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationResponseDTO {
    private Long id;
    private LocalDate appliedDate;
    private ApplicationStatus status;
    private String priority;
    private String rejectionReason;
    private String resumeUrl;
    private String feedback;
    private String notes;
    private Long candidateId;
    private String fullName;

    private String email;

    private String phone;
    private Long jobPositionId;

    public static ApplicationResponseDTO fromEntity(com.example.candidate_service.model.Application application) {
        ApplicationResponseDTO dto = new ApplicationResponseDTO();
        dto.setId(application.getId());
        dto.setAppliedDate(application.getAppliedDate());
        dto.setStatus(application.getStatus());
        dto.setPriority(application.getPriority());
        dto.setRejectionReason(application.getRejectionReason());
        dto.setResumeUrl(application.getResumeUrl());
        dto.setFeedback(application.getFeedback());
        dto.setNotes(application.getNotes());
        dto.setJobPositionId(application.getJobPositionId());
        dto.setFullName(application.getFullName());
        dto.setEmail(application.getEmail());
        dto.setPhone(application.getPhone());
        return dto;
    }
}
