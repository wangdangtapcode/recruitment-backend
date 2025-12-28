package com.example.statistics_service.controller;

import com.example.statistics_service.dto.statistics.*;
import com.example.statistics_service.service.StatisticsService;
import com.example.statistics_service.utils.SecurityUtil;
import com.example.statistics_service.utils.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/statistics-service/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * Lấy thống kê tổng quan với so sánh kỳ trước
     * GET /api/v1/statistics-service/statistics/summary
     * 
     * @param periodType WEEKLY (mặc định), MONTHLY, YEARLY
     */
    @GetMapping("/summary")
    @ApiMessage("Lấy thống kê tổng quan - Hồ sơ ứng tuyển, Tuyển, Phỏng vấn, Từ chối")
    public ResponseEntity<SummaryStatisticsDTO> getSummaryStatistics(
            @RequestParam(name = "periodType", defaultValue = "WEEKLY", required = false) String periodType) {
        Optional<String> tokenOpt = SecurityUtil.getCurrentUserJWT();
        String token = tokenOpt.orElse(null);

        SummaryStatisticsDTO statistics = statisticsService.getSummaryStatistics(token, periodType);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Lấy danh sách vị trí tuyển dụng
     * GET /api/v1/statistics-service/statistics/job-openings
     */
    @GetMapping("/job-openings")
    @ApiMessage("Lấy danh sách vị trí tuyển dụng với thông tin chi tiết")
    public ResponseEntity<List<JobOpeningDTO>> getJobOpenings(
            @RequestParam(name = "limit", defaultValue = "10", required = false) int limit) {
        Optional<String> tokenOpt = SecurityUtil.getCurrentUserJWT();
        String token = tokenOpt.orElse(null);

        List<JobOpeningDTO> jobOpenings = statisticsService.getJobOpenings(token, limit);
        return ResponseEntity.ok(jobOpenings);
    }

    /**
     * Lấy lịch phỏng vấn sắp tới
     * GET /api/v1/statistics-service/statistics/upcoming-schedules
     */
    @GetMapping("/upcoming-schedules")
    @ApiMessage("Lấy lịch phỏng vấn sắp tới")
    public ResponseEntity<UpcomingScheduleDTO> getUpcomingSchedules(
            @RequestParam(name = "limit", defaultValue = "10", required = false) int limit) {
        Optional<String> tokenOpt = SecurityUtil.getCurrentUserJWT();
        String token = tokenOpt.orElse(null);

        UpcomingScheduleDTO schedules = statisticsService.getUpcomingSchedules(token, limit);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Lấy dữ liệu biểu đồ chart về applications
     * GET /api/v1/statistics-service/statistics/applications/chart
     * 
     * Query params:
     * - chartType: TIMELINE, STATUS, DEPARTMENT, POSITION (mặc định: TIMELINE)
     * - period: Chuỗi mô tả kỳ thống kê (ví dụ: "30 ngày qua", "90 ngày qua", "7
     * tháng này", "tháng này", "năm này")
     * - periodType: WEEKLY, MONTHLY, YEARLY (optional, nếu có period thì không cần)
     */
    @GetMapping("/applications/chart")
    @ApiMessage("Lấy dữ liệu biểu đồ chart về applications")
    public ResponseEntity<ApplicationChartDTO> getApplicationChart(
            @RequestParam(name = "chartType", defaultValue = "TIMELINE", required = false) String chartType,
            @RequestParam(name = "period", required = false) String period,
            @RequestParam(name = "periodType", required = false) String periodType) {
        Optional<String> tokenOpt = SecurityUtil.getCurrentUserJWT();
        String token = tokenOpt.orElse(null);

        ApplicationChartDTO chart = statisticsService.getApplicationChart(token, chartType, period, periodType);
        return ResponseEntity.ok(chart);
    }
}
