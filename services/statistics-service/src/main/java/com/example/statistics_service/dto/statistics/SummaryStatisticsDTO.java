package com.example.statistics_service.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryStatisticsDTO {
    private String periodType; // WEEKLY, MONTHLY, YEARLY
    private StatisticItem applications; // Hồ sơ ứng tuyển
    private StatisticItem hired; // Tuyển
    private StatisticItem interviews; // Phỏng vấn
    private StatisticItem rejected; // Từ chối

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticItem {
        private Long value; // Giá trị hiện tại
        private Double changePercent; // % thay đổi so với kỳ trước
        private Boolean isIncrease; // true nếu tăng, false nếu giảm
    }
}
