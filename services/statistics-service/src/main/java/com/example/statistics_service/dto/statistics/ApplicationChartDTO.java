package com.example.statistics_service.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho biểu đồ chart về applications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationChartDTO {

    private String chartType; // "TIMELINE", "STATUS", "DEPARTMENT", "POSITION"
    private String periodType; // WEEKLY, MONTHLY, YEARLY
    private LocalDate periodStart;
    private LocalDate periodEnd;

    /**
     * Dữ liệu timeline - applications theo thời gian
     */
    private List<TimelineDataItem> timelineData;

    /**
     * Dữ liệu theo status - số lượng applications theo từng status
     */
    private List<StatusDataItem> statusData;

    /**
     * Dữ liệu theo department - số lượng applications theo từng phòng ban
     */
    private List<DepartmentDataItem> departmentData;

    /**
     * Dữ liệu theo position - số lượng applications theo từng vị trí
     */
    private List<PositionDataItem> positionData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineDataItem {
        private String label; // "T2", "T3", "Tháng 1", "Q1", etc.
        private String date; // YYYY-MM-DD
        private Long applications; // Số lượng applications
        private Long hired; // Số lượng hired
        private Long rejected; // Số lượng rejected
        private Long pending; // Số lượng pending
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusDataItem {
        private String status; // "HIRED", "REJECTED", "PENDING", "SUBMITTED", etc.
        private String statusLabel; // "Đã tuyển", "Từ chối", "Đang xử lý", etc.
        private Long count; // Số lượng
        private Double percentage; // Phần trăm
        private String color; // Màu sắc cho chart
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentDataItem {
        private Long departmentId;
        private String departmentName;
        private Long count; // Số lượng applications
        private Double percentage; // Phần trăm
        private String color; // Màu sắc cho chart
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionDataItem {
        private Long positionId;
        private String positionName;
        private Long count; // Số lượng applications
        private Double percentage; // Phần trăm
        private String color; // Màu sắc cho chart
    }
}
