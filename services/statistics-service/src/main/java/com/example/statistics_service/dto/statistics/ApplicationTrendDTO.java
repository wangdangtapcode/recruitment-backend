package com.example.statistics_service.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationTrendDTO {
    private String period;                   // "7 tháng này", "Tuần này", etc.
    private List<DailyTrendItem> dailyTrend; // Dữ liệu theo ngày (T2, T3, T4, T5, T6, T7, CN)

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrendItem {
        private String day;                  // "T2", "T3", "T4", "T5", "T6", "T7", "CN"
        private Long count;                  // Số lượng ứng tuyển trong ngày
        private String date;                 // Ngày thực tế (YYYY-MM-DD)
    }
}

