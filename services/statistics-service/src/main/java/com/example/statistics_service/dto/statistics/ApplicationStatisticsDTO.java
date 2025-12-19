package com.example.statistics_service.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO cho thống kê ứng viên và đơn ứng tuyển
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatisticsDTO {
    
    private String periodType; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    private Integer year;
    private Integer month;
    private Integer quarter;
    private Integer day;
    
    private Long totalCandidates;
    private Long totalApplications;
    private Map<String, Long> applicationsByStatus;
    private Map<String, Long> candidatesByStage;
    private Map<Long, Long> applicationsByPosition;
    private Map<Long, Long> applicationsByDepartment;
    
    private Double conversionRate;
    private Double averageProcessingTime;
    
    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    /**
     * So sánh với kỳ trước
     */
    private ApplicationComparisonDTO comparison;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationComparisonDTO {
        private Long previousTotalApplications;
        private Long previousTotalCandidates;
        private Double previousConversionRate;
        private Long applicationsChange;
        private Long candidatesChange;
        private Double conversionRateChange;
    }
}

