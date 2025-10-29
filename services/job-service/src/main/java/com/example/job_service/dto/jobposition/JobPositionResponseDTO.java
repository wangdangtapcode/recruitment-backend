package com.example.job_service.dto.jobposition;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.job_service.model.JobPosition;
import com.example.job_service.model.RecruitmentRequest;
import com.example.job_service.utils.enums.JobPositionStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobPositionResponseDTO {
    private Long id;
    private String title;
    private String description;
    // private String responsibilities;
    private String requirements;
    // private String qualifications;
    private String benefits;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    private String employmentType;
    private String experienceLevel;
    private String location;
    private boolean isRemote;
    private int quantity;
    private LocalDate deadline;

    private int applicationCount;
    private JobPositionStatus status;
    private RecruitmentRequest recruitmentRequest;
    private String departmentName; // Tên phòng ban
    private String yearsOfExperience;

    public static JobPositionResponseDTO fromEntity(JobPosition position) {
        JobPositionResponseDTO dto = new JobPositionResponseDTO();
        dto.setId(position.getId());
        dto.setTitle(position.getTitle());
        dto.setDescription(position.getDescription());
        // dto.setResponsibilities(position.getResponsibilities());
        dto.setRequirements(position.getRequirements());
        // dto.setQualifications(position.getQualifications());
        dto.setBenefits(position.getBenefits());
        dto.setSalaryMin(position.getSalaryMin());
        dto.setSalaryMax(position.getSalaryMax());
        dto.setCurrency(position.getCurrency());
        dto.setEmploymentType(position.getEmploymentType());
        dto.setExperienceLevel(position.getExperienceLevel());
        dto.setLocation(position.getLocation());
        dto.setRemote(position.isRemote());
        dto.setDeadline(position.getDeadline());
        dto.setQuantity(position.getQuantity());
        dto.setApplicationCount(position.getApplicationCount());
        dto.setStatus(position.getStatus());

        dto.setRecruitmentRequest(position.getRecruitmentRequest());

        // Set additional fields for UI display
        dto.setYearsOfExperience(position.getYearsOfExperience());

        return dto;
    }

}
