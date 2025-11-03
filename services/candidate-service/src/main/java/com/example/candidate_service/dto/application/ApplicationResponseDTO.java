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
    private Long jobPositionId;
    private String jobPositionTitle;
    private Long departmentId;
    private String departmentName; // filled by service via job-service
    private String fullName;
    private String email;
    private String phone;

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
        dto.setCandidateId(application.getCandidate() != null ? application.getCandidate().getId() : null);
        if (application.getCandidate() != null) {
            dto.setFullName(application.getCandidate().getFullName());
            dto.setEmail(application.getCandidate().getEmail());
            dto.setPhone(application.getCandidate().getPhone());
        }
        return dto;
    }
}
