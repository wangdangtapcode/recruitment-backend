package com.example.statistics_service.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO cho thống kê tuyển dụng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentStatisticsDTO {
    
    private String periodType; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    private Integer year;
    private Integer month;
    private Integer quarter;
    private Integer day;
    
    private Long totalRequests;
    private Map<String, Long> requestsByStatus;
    
    private Long totalJobPositions;
    private Long totalApplications;
    private Map<String, Long> applicationsByStatus;
    
    private Long hiredCount;
    private Double hireRate;
    
    private Map<Long, Long> requestsByDepartment;
    
    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    /**
     * So sánh với kỳ trước
     */
    private RecruitmentComparisonDTO comparison;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecruitmentComparisonDTO {
        private Long previousTotalRequests;
        private Long previousHiredCount;
        private Double previousHireRate;
        private Long requestsChange;
        private Long hiredChange;
        private Double hireRateChange;
    }
}

