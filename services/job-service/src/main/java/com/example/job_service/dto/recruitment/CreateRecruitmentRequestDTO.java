package com.example.job_service.dto.recruitment;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
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
    private String reason;
    private String description;
    private String benefits;
    private String requirements;
    private BigDecimal salaryMin; // Chỉ có giá trị khi isExceedBudget = true
    private BigDecimal salaryMax; // Chỉ có giá trị khi isExceedBudget = true
    private String currency; // Chỉ có giá trị khi isExceedBudget = true
    private String location;
    private boolean isExceedBudget;

    @NotNull
    private Long jobCategoryId;
    @NotNull
    private Long requesterId; // Employee Id
    @NotNull
    private Long departmentId;

    /**
     * Validation: Nếu vượt quỹ thì phải có salary
     */
    @AssertTrue(message = "Khi vượt quỹ, phải cung cấp thông tin lương")
    public boolean isValidSalaryWhenExceedBudget() {
        if (isExceedBudget) {
            return salaryMin != null && salaryMax != null && currency != null && !currency.trim().isEmpty();
        }
        return true;
    }
}
