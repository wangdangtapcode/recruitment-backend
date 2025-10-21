package com.example.job_service.dto.recruitment;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRecruitmentRequestDTO {
    @NotNull
    private String title;
    @NotNull
    private Integer numberOfPositions;
    private String priorityLevel;
    private String requestReason;
    private String jobDescription;
    private String requirements;
    private String preferredQualifications;
    private BigDecimal salaryRangeMin;
    private BigDecimal salaryRangeMax;
    private String currency;
    private String employmentType;
    private String workLocation;
    private LocalDate expectedStartDate;
    private LocalDate deadline;

    @NotNull
    private Long jobCategoryId;
    @NotNull
    private Long requesterId;
    @NotNull
    private Long departmentId;
}
