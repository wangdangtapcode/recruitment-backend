package com.example.job_service.dto.jobposition;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateJobPositionDTO {
    @NotNull
    private String title;
    private String description;
    private String responsibilities;
    private String requirements;
    private String preferredQualifications;
    private String benefits;
    private BigDecimal salaryRangeMin;
    private BigDecimal salaryRangeMax;
    private String currency;
    private String employmentType;
    private String experienceLevel;
    private String workLocation;
    private boolean remoteWorkAllowed;
    private int numberOfOpenings;
    private LocalDate applicationDeadline;

    @NotNull
    private Long recruitmentRequestId;

    @Getter
    @Setter
    public static class SkillRequirement {
        @NotNull
        private Long skillId;
        private String proficiencyLevel;
        private boolean required;
    }

    private List<SkillRequirement> skills;
}
