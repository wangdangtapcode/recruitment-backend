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
     * Lấy thống kê tổng quan với so sánh tuần trước
     * GET /api/v1/statistics-service/statistics/summary
     */
    @GetMapping("/summary")
    @ApiMessage("Lấy thống kê tổng quan - Hồ sơ ứng tuyển, Tuyển, Phỏng vấn, Từ chối")
    public ResponseEntity<SummaryStatisticsDTO> getSummaryStatistics() {
        Optional<String> tokenOpt = SecurityUtil.getCurrentUserJWT();
        String token = tokenOpt.orElse(null);

        SummaryStatisticsDTO statistics = statisticsService.getSummaryStatistics(token);
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
     * Lấy xu hướng ứng tuyển theo thời gian
     * GET /api/v1/statistics-service/statistics/application-trends
     */
    @GetMapping("/application-trends")
    @ApiMessage("Lấy xu hướng ứng tuyển theo 7 ngày trong tuần")
    public ResponseEntity<ApplicationTrendDTO> getApplicationTrends(
            @RequestParam(name = "period", defaultValue = "7 ngày này", required = false) String period) {
        Optional<String> tokenOpt = SecurityUtil.getCurrentUserJWT();
        String token = tokenOpt.orElse(null);

        ApplicationTrendDTO trends = statisticsService.getApplicationTrends(token, period);
        return ResponseEntity.ok(trends);
    }

    /**
     * Lấy phân bổ ứng tuyển theo vị trí
     * GET /api/v1/statistics-service/statistics/applications-by-position
     */
    @GetMapping("/applications-by-position")
    @ApiMessage("Lấy phân bổ ứng tuyển theo từng vị trí tuyển dụng")
    public ResponseEntity<ApplicationByPositionDTO> getApplicationsByPosition() {
        Optional<String> tokenOpt = SecurityUtil.getCurrentUserJWT();
        String token = tokenOpt.orElse(null);

        ApplicationByPositionDTO statistics = statisticsService.getApplicationsByPosition(token);
        return ResponseEntity.ok(statistics);
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

    // ===================================================================
    // CÁC API THỐNG KÊ MỚI VỚI MONGODB
    // ===================================================================

    /**
     * Lấy thống kê doanh thu theo tháng/quý/năm
     * GET /api/v1/statistics-service/statistics/revenue
     * 
     * Query params:
     * - periodType: MONTHLY, QUARTERLY, YEARLY (required)
     * - year: năm (required)
     * - month: tháng (1-12, required nếu periodType=MONTHLY)
     * - quarter: quý (1-4, required nếu periodType=QUARTERLY)
     */
    @GetMapping("/revenue")
    @ApiMessage("Lấy thống kê doanh thu theo tháng/quý/năm")
    public ResponseEntity<RevenueStatisticsDTO> getRevenueStatistics(
            @RequestParam(name = "periodType", required = true) String periodType,
            @RequestParam(name = "year", required = true) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "quarter", required = false) Integer quarter) {
        Optional<String> tokenOpt = SecurityUtil.getCurrentUserJWT();
        String token = tokenOpt.orElse(null);

        RevenueStatisticsDTO statistics = statisticsService.getRevenueStatistics(
                token, periodType, year, month, quarter);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Lấy thống kê tuyển dụng theo thời gian
     * GET /api/v1/statistics-service/statistics/recruitment
     * 
     * Query params:
     * - periodType: DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY (required)
     * - year: năm (required)
     * - month: tháng (1-12, required nếu periodType=DAILY hoặc MONTHLY)
     * - quarter: quý (1-4, required nếu periodType=QUARTERLY)
     * - day: ngày (1-31, required nếu periodType=DAILY)
     */
    @GetMapping("/recruitment")
    @ApiMessage("Lấy thống kê tuyển dụng theo thời gian")
    public ResponseEntity<RecruitmentStatisticsDTO> getRecruitmentStatistics(
            @RequestParam(name = "periodType", required = true) String periodType,
            @RequestParam(name = "year", required = true) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "quarter", required = false) Integer quarter,
            @RequestParam(name = "day", required = false) Integer day) {
        Optional<String> tokenOpt = SecurityUtil.getCurrentUserJWT();
        String token = tokenOpt.orElse(null);

        RecruitmentStatisticsDTO statistics = statisticsService.getRecruitmentStatistics(
                token, periodType, year, month, quarter, day);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Lấy thống kê ứng viên và đơn ứng tuyển
     * GET /api/v1/statistics-service/statistics/applications
     * 
     * Query params:
     * - periodType: DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY (required)
     * - year: năm (required)
     * - month: tháng (1-12, required nếu periodType=DAILY hoặc MONTHLY)
     * - quarter: quý (1-4, required nếu periodType=QUARTERLY)
     * - day: ngày (1-31, required nếu periodType=DAILY)
     */
    @GetMapping("/applications")
    @ApiMessage("Lấy thống kê ứng viên và đơn ứng tuyển")
    public ResponseEntity<ApplicationStatisticsDTO> getApplicationStatistics(
            @RequestParam(name = "periodType", required = true) String periodType,
            @RequestParam(name = "year", required = true) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "quarter", required = false) Integer quarter,
            @RequestParam(name = "day", required = false) Integer day) {
        Optional<String> tokenOpt = SecurityUtil.getCurrentUserJWT();
        String token = tokenOpt.orElse(null);

        ApplicationStatisticsDTO statistics = statisticsService.getApplicationStatistics(
                token, periodType, year, month, quarter, day);
        return ResponseEntity.ok(statistics);
    }
}
