package com.example.statistics_service.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * DTO cho thống kê doanh thu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatisticsDTO {
    
    private String periodType; // MONTHLY, QUARTERLY, YEARLY
    private Integer year;
    private Integer month;
    private Integer quarter;
    
    private BigDecimal totalRevenue;
    private Long hiredCount;
    private BigDecimal averageRevenuePerEmployee;
    
    private Map<Long, BigDecimal> revenueByDepartment;
    private Map<Long, BigDecimal> revenueByPosition;
    
    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    /**
     * So sánh với kỳ trước
     */
    private RevenueComparisonDTO comparison;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueComparisonDTO {
        private BigDecimal previousTotalRevenue;
        private BigDecimal revenueChange;
        private Double revenueChangePercent;
        private Boolean isIncrease;
    }
}

