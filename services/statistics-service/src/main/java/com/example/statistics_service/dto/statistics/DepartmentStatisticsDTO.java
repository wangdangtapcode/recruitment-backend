package com.example.statistics_service.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO cho thống kê theo phòng ban
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatisticsDTO {
    
    private Long departmentId;
    private String departmentName;
    private String periodType; // MONTHLY, QUARTERLY, YEARLY
    private Integer year;
    private Integer month;
    private Integer quarter;
    
    private Long totalEmployees;
    private Long totalRecruitmentRequests;
    private Long totalJobPositions;
    private Long totalApplications;
    private Long hiredCount;
    private BigDecimal totalRevenue;
    private Long totalInterviews;
    private Double hireRate;
    
    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    /**
     * So sánh với kỳ trước
     */
    private DepartmentComparisonDTO comparison;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentComparisonDTO {
        private Long previousHiredCount;
        private BigDecimal previousTotalRevenue;
        private Double previousHireRate;
        private Long hiredChange;
        private BigDecimal revenueChange;
        private Double hireRateChange;
    }
}

