package com.example.statistics_service.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO cho thống kê phỏng vấn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewStatisticsDTO {
    
    private String periodType; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    private Integer year;
    private Integer month;
    private Integer quarter;
    private Integer day;
    
    private Long totalInterviews;
    private Map<String, Long> interviewsByStatus;
    private Map<String, Long> interviewsByType;
    private Map<Long, Long> interviewsByDepartment;
    
    private Double completionRate;
    private Double cancellationRate;
    private Double averageRating;
    
    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    /**
     * So sánh với kỳ trước
     */
    private InterviewComparisonDTO comparison;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterviewComparisonDTO {
        private Long previousTotalInterviews;
        private Double previousCompletionRate;
        private Double previousAverageRating;
        private Long interviewsChange;
        private Double completionRateChange;
        private Double averageRatingChange;
    }
}

