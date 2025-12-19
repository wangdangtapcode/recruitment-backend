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
public class ApplicationByPositionDTO {
    private List<PositionStatistic> positions; // Danh sách vị trí với thống kê

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionStatistic {
        private Long jobPositionId;
        private String jobTitle;              // Tên vị trí
        private Double percentage;            // Phần trăm (12.5%, 31.25%)
        private Long applicationCount;        // Số hồ sơ ứng tuyển
        private String barColor;              // Màu progress bar (purple, pink, green)
    }
}

