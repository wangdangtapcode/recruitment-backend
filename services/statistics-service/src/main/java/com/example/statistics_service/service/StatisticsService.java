package com.example.statistics_service.service;

import com.example.statistics_service.dto.PaginationDTO;
import com.example.statistics_service.dto.statistics.*;
import com.example.statistics_service.service.client.*;
import com.example.statistics_service.utils.SecurityUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

        private final UserServiceClient userServiceClient;
        private final JobServiceClient jobServiceClient;
        private final CandidateServiceClient candidateServiceClient;
        private final ScheduleServiceClient communicationServiceClient;
        private final ObjectMapper objectMapper = new ObjectMapper();

        private Long getTotalFromPagination(PaginationDTO pagination) {
                if (pagination == null || pagination.getMeta() == null) {
                        return 0L;
                }
                return pagination.getMeta().getTotal();
        }

        /**
         * Lấy thống kê tổng quan với so sánh kỳ trước
         * GET /api/v1/statistics-service/statistics/summary
         * 
         * @param token      JWT token
         * @param periodType WEEKLY, MONTHLY, YEARLY (mặc định: WEEKLY)
         */
        // @Cacheable(value = "dashboard-stats", key = "'summary-' + #token + '-' +
        // #periodType")
        public SummaryStatisticsDTO getSummaryStatistics(String token, String periodType) {
                log.info("Fetching summary statistics for period: {}", periodType);

                if (periodType == null || periodType.isEmpty()) {
                        periodType = "WEEKLY";
                }

                LocalDate today = LocalDate.now();
                LocalDate periodStart;
                LocalDate periodEnd;
                LocalDate previousPeriodStart;
                LocalDate previousPeriodEnd;

                switch (periodType.toUpperCase()) {
                        case "MONTHLY": {
                                YearMonth currentMonth = YearMonth.from(today);
                                periodStart = currentMonth.atDay(1);
                                periodEnd = currentMonth.atEndOfMonth();

                                YearMonth previousMonth = currentMonth.minusMonths(1);
                                previousPeriodStart = previousMonth.atDay(1);
                                previousPeriodEnd = previousMonth.atEndOfMonth();
                                break;
                        }
                        case "YEARLY": {
                                periodStart = LocalDate.of(today.getYear(), 1, 1);
                                periodEnd = LocalDate.of(today.getYear(), 12, 31);

                                previousPeriodStart = LocalDate.of(today.getYear() - 1, 1, 1);
                                previousPeriodEnd = LocalDate.of(today.getYear() - 1, 12, 31);
                                break;
                        }
                        default: // WEEKLY
                        {
                                LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1); // Thứ 2
                                periodStart = weekStart;
                                periodEnd = weekStart.plusDays(6); // Chủ nhật

                                previousPeriodStart = weekStart.minusWeeks(1);
                                previousPeriodEnd = previousPeriodStart.plusDays(6);
                                break;
                        }
                }

                // Lấy departmentId dựa trên role (CEO xem tất cả, MANAGER/STAFF xem phòng ban
                // của họ)
                Long departmentId = getDepartmentIdForStatistics();
                // Gọi 1 lần API để lấy dữ liệu cho cả 2 kỳ (từ previousPeriodStart đến
                // periodEnd)
                String startDateStr = previousPeriodStart.format(DateTimeFormatter.ISO_DATE);
                String endDateStr = periodEnd.format(DateTimeFormatter.ISO_DATE);

                List<JsonNode> allApplications = candidateServiceClient.getApplicationsForStatistics(
                                token, null, startDateStr, endDateStr, null, departmentId);
                List<JsonNode> allSchedules = communicationServiceClient.getSchedulesForStatistics(
                                token, previousPeriodStart, periodEnd, null, null);
                // Filter dữ liệu kỳ hiện tại
                Long currentApplications = (long) filterApplicationsByDateRange(allApplications, periodStart, periodEnd)
                                .size();
                Long currentHired = (long) filterApplicationsByDateRangeAndStatus(allApplications, periodStart,
                                periodEnd, "HIRED").size();
                Long currentRejected = (long) filterApplicationsByDateRangeAndStatus(allApplications, periodStart,
                                periodEnd, "REJECTED").size();
                Long currentInterviews = (long) filterSchedulesByDateRange(allSchedules, periodStart, periodEnd).size();

                // Filter dữ liệu kỳ trước
                Long previousApplications = (long) filterApplicationsByDateRange(allApplications, previousPeriodStart,
                                previousPeriodEnd).size();
                Long previousHired = (long) filterApplicationsByDateRangeAndStatus(allApplications, previousPeriodStart,
                                previousPeriodEnd, "HIRED").size();
                Long previousRejected = (long) filterApplicationsByDateRangeAndStatus(allApplications,
                                previousPeriodStart, previousPeriodEnd, "REJECTED").size();
                Long previousInterviews = (long) filterSchedulesByDateRange(allSchedules, previousPeriodStart,
                                previousPeriodEnd).size();

                return SummaryStatisticsDTO.builder()
                                .periodType(periodType.toUpperCase())
                                .applications(calculateStatisticItem(currentApplications, previousApplications))
                                .hired(calculateStatisticItem(currentHired, previousHired))
                                .interviews(calculateStatisticItem(currentInterviews, previousInterviews))
                                .rejected(calculateStatisticItem(currentRejected, previousRejected))
                                .build();
        }

        /**
         * Lấy danh sách vị trí tuyển dụng
         * GET /api/v1/statistics-service/statistics/job-openings
         */
        @Cacheable(value = "dashboard-stats", key = "'job-openings-' + #token")
        public List<JobOpeningDTO> getJobOpenings(String token, int limit) {
                log.info("Fetching job openings");

                PaginationDTO jobPositions = jobServiceClient.getJobPositions(token, 1, limit);
                if (jobPositions == null || jobPositions.getResult() == null) {
                        return new ArrayList<>();
                }

                @SuppressWarnings("unchecked")
                List<Object> positions = (List<Object>) jobPositions.getResult();
                String[] colors = { "purple", "green", "pink", "darkgreen", "blue", "orange", "yellow" };

                List<JobOpeningDTO> result = new ArrayList<>();
                for (int i = 0; i < Math.min(positions.size(), limit); i++) {
                        JsonNode pos = convertToJsonNode(positions.get(i));
                        if (pos == null)
                                continue;
                        Long jobPositionId = pos.has("id") ? pos.get("id").asLong() : null;
                        String title = pos.has("title") ? pos.get("title").asText() : "";
                        String employmentType = pos.has("employmentType") ? pos.get("employmentType").asText()
                                        : "Full-time";
                        String location = pos.has("location") ? pos.get("location").asText() : "On-site";
                        boolean isRemote = pos.has("isRemote") && pos.get("isRemote").asBoolean();
                        String workLocation = isRemote ? "Remote"
                                        : (location.contains("Hybrid") ? "Hybrid" : "On-site");

                        BigDecimal salaryMin = pos.has("salaryMin") && !pos.get("salaryMin").isNull()
                                        ? new BigDecimal(pos.get("salaryMin").asText())
                                        : null;
                        BigDecimal salaryMax = pos.has("salaryMax") && !pos.get("salaryMax").isNull()
                                        ? new BigDecimal(pos.get("salaryMax").asText())
                                        : null;

                        // Lấy số ứng viên
                        Long applicantCount = 0L;
                        if (jobPositionId != null) {
                                applicantCount = getApplicationCountByJobPosition(token, jobPositionId);
                        }

                        String salaryDisplay = formatSalary(salaryMin, salaryMax);

                        result.add(JobOpeningDTO.builder()
                                        .id(jobPositionId)
                                        .title(title)
                                        .iconColor(colors[i % colors.length])
                                        .employmentType(employmentType)
                                        .workLocation(workLocation)
                                        .applicantCount(applicantCount.intValue())
                                        .salaryMin(salaryMin)
                                        .salaryMax(salaryMax)
                                        .salaryDisplay(salaryDisplay)
                                        .build());
                }
                return result;
        }

        /**
         * Lấy lịch phỏng vấn sắp tới
         * GET /api/v1/statistics-service/statistics/upcoming-schedules
         */
        @Cacheable(value = "dashboard-stats", key = "'upcoming-schedules-' + #token")
        public UpcomingScheduleDTO getUpcomingSchedules(String token, int limit) {
                log.info("Fetching upcoming schedules");

                List<JsonNode> schedules = communicationServiceClient.getUpcomingSchedules(token, limit);

                List<UpcomingScheduleDTO.ScheduleItem> scheduleItems = schedules.stream()
                                .limit(limit)
                                .map(scheduleObj -> {
                                        JsonNode schedule = convertToJsonNode(scheduleObj);
                                        if (schedule == null)
                                                return null;

                                        Long scheduleId = schedule.has("id") ? schedule.get("id").asLong() : null;

                                        // Parse startTime
                                        String startTimeStr = schedule.has("startTime")
                                                        ? schedule.get("startTime").asText()
                                                        : "";
                                        LocalDateTime startTime = parseDateTime(startTimeStr);
                                        String time = startTime != null
                                                        ? startTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                                                        : "";
                                        String date = startTime != null
                                                        ? startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                                        : "";

                                        // Lấy thông tin job và candidate từ schedule
                                        String jobTitle = schedule.has("title") ? schedule.get("title").asText() : "";
                                        String candidateName = "";
                                        if (schedule.has("participants")) {
                                                JsonNode participants = schedule.get("participants");
                                                if (participants.isArray() && participants.size() > 0) {
                                                        JsonNode firstParticipant = participants.get(0);
                                                        if (firstParticipant.has("name")) {
                                                                candidateName = firstParticipant.get("name").asText();
                                                        }
                                                }
                                        }

                                        String type = schedule.has("meetingType") ? schedule.get("meetingType").asText()
                                                        : "Phỏng vấn";
                                        String status = schedule.has("status") ? schedule.get("status").asText() : "";

                                        return UpcomingScheduleDTO.ScheduleItem.builder()
                                                        .scheduleId(scheduleId)
                                                        .time(time)
                                                        .jobTitle(jobTitle)
                                                        .candidateName(candidateName)
                                                        .type(type)
                                                        .status(status)
                                                        .date(date)
                                                        .build();
                                })
                                .filter(item -> item != null)
                                .collect(Collectors.toList());

                return UpcomingScheduleDTO.builder()
                                .schedules(scheduleItems)
                                .build();
        }

        // ===================================================================
        // HELPER METHODS
        // ===================================================================

        private SummaryStatisticsDTO.StatisticItem calculateStatisticItem(Long current, Long previous) {
                if (previous == null || previous == 0) {
                        return SummaryStatisticsDTO.StatisticItem.builder()
                                        .value(current)
                                        .changePercent(0.0)
                                        .isIncrease(null)
                                        .build();
                }

                Double changePercent = ((current.doubleValue() - previous.doubleValue()) / previous.doubleValue())
                                * 100;
                Boolean isIncrease = current > previous;

                return SummaryStatisticsDTO.StatisticItem.builder()
                                .value(current)
                                .changePercent(Math.round(changePercent * 100.0) / 100.0)
                                .isIncrease(isIncrease)
                                .build();
        }

        private String formatSalary(BigDecimal min, BigDecimal max) {
                if (min == null && max == null)
                        return "";
                if (min == null)
                        return formatVND(max) + " triệu";
                if (max == null)
                        return formatVND(min) + " triệu";
                if (min.equals(max))
                        return formatVND(min) + " triệu";
                return formatVND(min) + " - " + formatVND(max) + " triệu";
        }

        private String formatVND(BigDecimal amount) {
                if (amount == null)
                        return "0";
                long millions = amount.longValue() / 1_000_000;
                if (millions > 0) {
                        return String.valueOf(millions);
                }
                long thousands = amount.longValue() / 1_000;
                return String.valueOf(thousands);
        }

        private Long getApplicationCountByJobPosition(String token, Long jobPositionId) {
                PaginationDTO apps = candidateServiceClient.getApplications(token, null, null, jobPositionId, 1, 1);
                return getTotalFromPagination(apps);
        }

        /**
         * Lấy departmentId dựa trên role để filter dữ liệu
         * CEO: null (xem tất cả)
         * MANAGER/STAFF: departmentId của họ
         */
        private Long getDepartmentIdForStatistics() {
                String role = SecurityUtil.extractUserRole();
                if (role == null) {
                        return null;
                }

                switch (role.toUpperCase()) {
                        case "CEO":
                        case "ADMIN":
                                // Xem tất cả phòng ban
                                return null;
                        case "MANAGER":
                        case "STAFF":
                                // Chỉ xem phòng ban của mình
                                return SecurityUtil.extractDepartmentId();
                        default:
                                return null;
                }
        }

        /**
         * Filter applications theo date range
         */
        private List<JsonNode> filterApplicationsByDateRange(List<JsonNode> applications, LocalDate start,
                        LocalDate end) {
                return applications.stream()
                                .filter(app -> {
                                        try {
                                                if (app.has("appliedDate")) {
                                                        String appliedDateStr = app.get("appliedDate").asText();
                                                        LocalDate appliedDate = LocalDate.parse(appliedDateStr);
                                                        return !appliedDate.isBefore(start)
                                                                        && !appliedDate.isAfter(end);
                                                }
                                        } catch (Exception e) {
                                                log.warn("Error parsing application date: {}", e.getMessage());
                                        }
                                        return false;
                                })
                                .collect(Collectors.toList());
        }

        /**
         * Filter applications theo date range và status
         */
        private List<JsonNode> filterApplicationsByDateRangeAndStatus(List<JsonNode> applications,
                        LocalDate start, LocalDate end, String status) {
                return applications.stream()
                                .filter(app -> {
                                        try {
                                                // Check status
                                                if (!app.has("status") || !status.equals(app.get("status").asText())) {
                                                        return false;
                                                }
                                                // Check date
                                                if (app.has("appliedDate")) {
                                                        String appliedDateStr = app.get("appliedDate").asText();
                                                        LocalDate appliedDate = LocalDate.parse(appliedDateStr);
                                                        return !appliedDate.isBefore(start)
                                                                        && !appliedDate.isAfter(end);
                                                }
                                        } catch (Exception e) {
                                                log.warn("Error parsing application: {}", e.getMessage());
                                        }
                                        return false;
                                })
                                .collect(Collectors.toList());
        }

        /**
         * Filter schedules theo date range
         */
        private List<JsonNode> filterSchedulesByDateRange(List<JsonNode> schedules, LocalDate start, LocalDate end) {
                return schedules.stream()
                                .filter(schedule -> {
                                        try {
                                                if (schedule.has("startTime")) {
                                                        String startTimeStr = schedule.get("startTime").asText();
                                                        LocalDateTime startTime = LocalDateTime.parse(startTimeStr,
                                                                        DateTimeFormatter.ISO_DATE_TIME);
                                                        LocalDate scheduleDate = startTime.toLocalDate();
                                                        return !scheduleDate.isBefore(start)
                                                                        && !scheduleDate.isAfter(end);
                                                }
                                        } catch (Exception e) {
                                                log.warn("Error parsing schedule date: {}", e.getMessage());
                                        }
                                        return false;
                                })
                                .collect(Collectors.toList());
        }

        private LocalDateTime parseDateTime(String dateTimeStr) {
                try {
                        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
                } catch (Exception e) {
                        try {
                                return LocalDate.parse(dateTimeStr, DateTimeFormatter.ISO_DATE).atStartOfDay();
                        } catch (Exception ex) {
                                return null;
                        }
                }
        }

        /**
         * Convert Object (LinkedHashMap) to JsonNode
         */
        private JsonNode convertToJsonNode(Object obj) {
                if (obj == null) {
                        return null;
                }
                if (obj instanceof JsonNode) {
                        return (JsonNode) obj;
                }
                try {
                        return objectMapper.valueToTree(obj);
                } catch (Exception e) {
                        log.warn("Error converting object to JsonNode: {}", e.getMessage());
                        return null;
                }
        }

        /**
         * Parse period string thành PeriodInfo
         * Hỗ trợ: "X ngày qua", "X tháng này", "tháng này", "năm này", etc.
         */
        private PeriodInfo parsePeriodString(String period) {
                if (period == null || period.isEmpty()) {
                        return null;
                }

                period = period.toLowerCase().trim();
                LocalDate today = LocalDate.now();

                // Pattern: "X ngày qua" hoặc "X ngày trước"
                if (period.contains("ngày") && (period.contains("qua") || period.contains("trước"))) {
                        try {
                                String numberStr = period.replaceAll("[^0-9]", "");
                                if (!numberStr.isEmpty()) {
                                        int days = Integer.parseInt(numberStr);
                                        LocalDate start = today.minusDays(days - 1);
                                        return new PeriodInfo(start, today, "DAILY");
                                }
                        } catch (NumberFormatException e) {
                                log.warn("Cannot parse days from period: {}", period);
                        }
                }

                // Pattern: "X tháng này" hoặc "tháng này"
                if (period.contains("tháng") && period.contains("này")) {
                        try {
                                String numberStr = period.replaceAll("[^0-9]", "");
                                if (numberStr.isEmpty()) {
                                        // "tháng này" = tháng hiện tại
                                        YearMonth currentMonth = YearMonth.from(today);
                                        LocalDate start = currentMonth.atDay(1);
                                        LocalDate end = currentMonth.atEndOfMonth();
                                        return new PeriodInfo(start, end, "MONTHLY");
                                } else {
                                        // "X tháng này" = X tháng gần nhất
                                        int months = Integer.parseInt(numberStr);
                                        YearMonth endMonth = YearMonth.from(today);
                                        YearMonth startMonth = endMonth.minusMonths(months - 1);
                                        LocalDate start = startMonth.atDay(1);
                                        LocalDate end = endMonth.atEndOfMonth();
                                        return new PeriodInfo(start, end, "MONTHLY");
                                }
                        } catch (NumberFormatException e) {
                                log.warn("Cannot parse months from period: {}", period);
                        }
                }

                // Pattern: "năm này"
                if (period.contains("năm") && period.contains("này")) {
                        LocalDate start = LocalDate.of(today.getYear(), 1, 1);
                        LocalDate end = LocalDate.of(today.getYear(), 12, 31);
                        return new PeriodInfo(start, end, "YEARLY");
                }

                // Pattern: "tuần này"
                if (period.contains("tuần") && period.contains("này")) {
                        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
                        LocalDate weekEnd = weekStart.plusDays(6);
                        return new PeriodInfo(weekStart, weekEnd, "WEEKLY");
                }

                log.warn("Unknown period format: {}", period);
                return null;
        }

        /**
         * Helper class để lưu thông tin period
         */
        private static class PeriodInfo {
                LocalDate start;
                LocalDate end;
                String periodType;

                PeriodInfo(LocalDate start, LocalDate end, String periodType) {
                        this.start = start;
                        this.end = end;
                        this.periodType = periodType;
                }
        }

        /**
         * Lấy dữ liệu biểu đồ chart về applications
         * GET /api/v1/statistics-service/statistics/applications/chart
         * 
         * @param token      JWT token
         * @param chartType  TIMELINE, STATUS, DEPARTMENT, POSITION
         * @param period     Chuỗi mô tả kỳ thống kê (ví dụ: "30 ngày qua", "tháng này")
         * @param periodType WEEKLY, MONTHLY, YEARLY
         */
        public ApplicationChartDTO getApplicationChart(String token, String chartType, String period,
                        String periodType) {
                log.info("Getting application chart: chartType={}, period={}, periodType={}", chartType, period,
                                periodType);

                if (chartType == null || chartType.isEmpty()) {
                        chartType = "TIMELINE";
                }

                // Parse period để lấy date range
                PeriodInfo periodInfo = null;
                if (period != null && !period.isEmpty()) {
                        periodInfo = parsePeriodString(period);
                }

                // Nếu không có period, dùng periodType
                if (periodInfo == null) {
                        LocalDate today = LocalDate.now();
                        if (periodType == null || periodType.isEmpty()) {
                                periodType = "MONTHLY";
                        }

                        switch (periodType.toUpperCase()) {
                                case "WEEKLY": {
                                        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
                                        periodInfo = new PeriodInfo(weekStart, weekStart.plusDays(6), "WEEKLY");
                                        break;
                                }
                                case "YEARLY": {
                                        periodInfo = new PeriodInfo(LocalDate.of(today.getYear(), 1, 1),
                                                        LocalDate.of(today.getYear(), 12, 31), "YEARLY");
                                        break;
                                }
                                default: // MONTHLY
                                {
                                        YearMonth currentMonth = YearMonth.from(today);
                                        periodInfo = new PeriodInfo(currentMonth.atDay(1), currentMonth.atEndOfMonth(),
                                                        "MONTHLY");
                                        break;
                                }
                        }
                }

                // Lấy departmentId dựa trên role
                Long departmentId = getDepartmentIdForStatistics();

                // Gọi API statistics để lấy dữ liệu
                String startDateStr = periodInfo.start.format(DateTimeFormatter.ISO_DATE);
                String endDateStr = periodInfo.end.format(DateTimeFormatter.ISO_DATE);

                List<JsonNode> allApplications = candidateServiceClient.getApplicationsForStatistics(
                                token, null, startDateStr, endDateStr, null, departmentId);

                ApplicationChartDTO.ApplicationChartDTOBuilder chartBuilder = ApplicationChartDTO.builder()
                                .chartType(chartType.toUpperCase())
                                .periodType(periodInfo.periodType)
                                .periodStart(periodInfo.start)
                                .periodEnd(periodInfo.end);

                // Tính toán dữ liệu theo chartType
                switch (chartType.toUpperCase()) {
                        case "STATUS":
                                chartBuilder.statusData(calculateStatusChartData(allApplications));
                                break;
                        case "DEPARTMENT":
                                chartBuilder.departmentData(calculateDepartmentChartData(allApplications, token));
                                break;
                        case "POSITION":
                                chartBuilder.positionData(calculatePositionChartData(allApplications, token));
                                break;
                        default: // TIMELINE
                                chartBuilder.timelineData(calculateTimelineChartData(allApplications, periodInfo));
                                break;
                }

                return chartBuilder.build();
        }

        /**
         * Tính toán dữ liệu timeline chart
         */
        private List<ApplicationChartDTO.TimelineDataItem> calculateTimelineChartData(List<JsonNode> applications,
                        PeriodInfo periodInfo) {
                List<ApplicationChartDTO.TimelineDataItem> timelineData = new ArrayList<>();
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(periodInfo.start, periodInfo.end);

                if (daysBetween <= 7) {
                        // Daily data cho tuần
                        String[] dayNames = { "CN", "T2", "T3", "T4", "T5", "T6", "T7" };
                        LocalDate current = periodInfo.start;
                        while (!current.isAfter(periodInfo.end)) {
                                final LocalDate date = current;
                                List<JsonNode> dayApps = applications.stream()
                                                .filter(app -> {
                                                        try {
                                                                if (app.has("appliedDate")) {
                                                                        LocalDate appliedDate = LocalDate
                                                                                        .parse(app.get("appliedDate")
                                                                                                        .asText());
                                                                        return appliedDate.equals(date);
                                                                }
                                                        } catch (Exception e) {
                                                                log.warn("Error parsing date: {}", e.getMessage());
                                                        }
                                                        return false;
                                                })
                                                .collect(Collectors.toList());

                                Long total = (long) dayApps.size();
                                Long hired = (long) dayApps.stream()
                                                .filter(app -> app.has("status")
                                                                && "HIRED".equals(app.get("status").asText()))
                                                .count();
                                Long rejected = (long) dayApps.stream()
                                                .filter(app -> app.has("status")
                                                                && "REJECTED".equals(app.get("status").asText()))
                                                .count();
                                Long pending = (long) dayApps.stream()
                                                .filter(app -> app.has("status")
                                                                && "PENDING".equals(app.get("status").asText()))
                                                .count();

                                int dayOfWeek = current.getDayOfWeek().getValue();
                                String dayName = dayNames[dayOfWeek % 7];

                                timelineData.add(ApplicationChartDTO.TimelineDataItem.builder()
                                                .label(dayName)
                                                .date(current.format(DateTimeFormatter.ISO_DATE))
                                                .applications(total)
                                                .hired(hired)
                                                .rejected(rejected)
                                                .pending(pending)
                                                .build());

                                current = current.plusDays(1);
                        }
                } else if (daysBetween <= 31) {
                        // Weekly data cho tháng
                        LocalDate weekStart = periodInfo.start;
                        int weekNumber = 1;
                        while (!weekStart.isAfter(periodInfo.end)) {
                                LocalDate weekEnd = weekStart.plusDays(6);
                                if (weekEnd.isAfter(periodInfo.end)) {
                                        weekEnd = periodInfo.end;
                                }

                                final LocalDate start = weekStart;
                                final LocalDate end = weekEnd;
                                List<JsonNode> weekApps = applications.stream()
                                                .filter(app -> {
                                                        try {
                                                                if (app.has("appliedDate")) {
                                                                        LocalDate appliedDate = LocalDate
                                                                                        .parse(app.get("appliedDate")
                                                                                                        .asText());
                                                                        return !appliedDate.isBefore(start)
                                                                                        && !appliedDate.isAfter(end);
                                                                }
                                                        } catch (Exception e) {
                                                                log.warn("Error parsing date: {}", e.getMessage());
                                                        }
                                                        return false;
                                                })
                                                .collect(Collectors.toList());

                                Long total = (long) weekApps.size();
                                Long hired = (long) weekApps.stream()
                                                .filter(app -> app.has("status")
                                                                && "HIRED".equals(app.get("status").asText()))
                                                .count();
                                Long rejected = (long) weekApps.stream()
                                                .filter(app -> app.has("status")
                                                                && "REJECTED".equals(app.get("status").asText()))
                                                .count();
                                Long pending = (long) weekApps.stream()
                                                .filter(app -> app.has("status")
                                                                && "PENDING".equals(app.get("status").asText()))
                                                .count();

                                timelineData.add(ApplicationChartDTO.TimelineDataItem.builder()
                                                .label("Tuần " + weekNumber)
                                                .date(weekStart.format(DateTimeFormatter.ISO_DATE) + " - "
                                                                + weekEnd.format(DateTimeFormatter.ISO_DATE))
                                                .applications(total)
                                                .hired(hired)
                                                .rejected(rejected)
                                                .pending(pending)
                                                .build());

                                weekStart = weekStart.plusWeeks(1);
                                weekNumber++;
                        }
                } else {
                        // Monthly data cho năm
                        LocalDate monthStart = periodInfo.start;
                        while (!monthStart.isAfter(periodInfo.end)) {
                                YearMonth yearMonth = YearMonth.from(monthStart);
                                LocalDate monthEnd = yearMonth.atEndOfMonth();
                                if (monthEnd.isAfter(periodInfo.end)) {
                                        monthEnd = periodInfo.end;
                                }

                                final LocalDate start = monthStart;
                                final LocalDate end = monthEnd;
                                List<JsonNode> monthApps = applications.stream()
                                                .filter(app -> {
                                                        try {
                                                                if (app.has("appliedDate")) {
                                                                        LocalDate appliedDate = LocalDate
                                                                                        .parse(app.get("appliedDate")
                                                                                                        .asText());
                                                                        return !appliedDate.isBefore(start)
                                                                                        && !appliedDate.isAfter(end);
                                                                }
                                                        } catch (Exception e) {
                                                                log.warn("Error parsing date: {}", e.getMessage());
                                                        }
                                                        return false;
                                                })
                                                .collect(Collectors.toList());

                                Long total = (long) monthApps.size();
                                Long hired = (long) monthApps.stream()
                                                .filter(app -> app.has("status")
                                                                && "HIRED".equals(app.get("status").asText()))
                                                .count();
                                Long rejected = (long) monthApps.stream()
                                                .filter(app -> app.has("status")
                                                                && "REJECTED".equals(app.get("status").asText()))
                                                .count();
                                Long pending = (long) monthApps.stream()
                                                .filter(app -> app.has("status")
                                                                && "PENDING".equals(app.get("status").asText()))
                                                .count();

                                timelineData.add(ApplicationChartDTO.TimelineDataItem.builder()
                                                .label("Tháng " + yearMonth.getMonthValue())
                                                .date(monthStart.format(DateTimeFormatter.ISO_DATE))
                                                .applications(total)
                                                .hired(hired)
                                                .rejected(rejected)
                                                .pending(pending)
                                                .build());

                                monthStart = yearMonth.plusMonths(1).atDay(1);
                        }
                }

                return timelineData;
        }

        /**
         * Tính toán dữ liệu status chart
         */
        private List<ApplicationChartDTO.StatusDataItem> calculateStatusChartData(List<JsonNode> applications) {
                Map<String, Long> statusCount = new HashMap<>();
                String[] colors = { "#10B981", "#EF4444", "#F59E0B", "#3B82F6", "#8B5CF6" };
                Map<String, String> statusLabels = new HashMap<>();
                statusLabels.put("HIRED", "Đã tuyển");
                statusLabels.put("REJECTED", "Từ chối");
                statusLabels.put("PENDING", "Đang xử lý");
                statusLabels.put("SUBMITTED", "Đã nộp");
                statusLabels.put("INTERVIEWED", "Đã phỏng vấn");

                // Đếm theo status
                for (JsonNode app : applications) {
                        if (app.has("status")) {
                                String status = app.get("status").asText();
                                statusCount.merge(status, 1L, Long::sum);
                        }
                }

                Long total = (long) applications.size();
                List<ApplicationChartDTO.StatusDataItem> statusData = new ArrayList<>();
                int colorIndex = 0;

                for (Map.Entry<String, Long> entry : statusCount.entrySet()) {
                        String status = entry.getKey();
                        Long count = entry.getValue();
                        Double percentage = total > 0 ? (count.doubleValue() / total.doubleValue()) * 100 : 0.0;

                        statusData.add(ApplicationChartDTO.StatusDataItem.builder()
                                        .status(status)
                                        .statusLabel(statusLabels.getOrDefault(status, status))
                                        .count(count)
                                        .percentage(Math.round(percentage * 100.0) / 100.0)
                                        .color(colors[colorIndex % colors.length])
                                        .build());
                        colorIndex++;
                }

                // Sắp xếp giảm dần theo count
                statusData.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));

                return statusData;
        }

        /**
         * Tính toán dữ liệu department chart
         */
        private List<ApplicationChartDTO.DepartmentDataItem> calculateDepartmentChartData(List<JsonNode> applications,
                        String token) {
                Map<Long, Long> deptCount = new HashMap<>();
                Map<Long, String> deptNames = new HashMap<>();
                String[] colors = { "#8B5CF6", "#EC4899", "#10B981", "#3B82F6", "#F59E0B", "#EF4444", "#6366F1" };

                // Đếm theo department
                for (JsonNode app : applications) {
                        if (app.has("departmentId") && !app.get("departmentId").isNull()) {
                                Long deptId = app.get("departmentId").asLong();
                                deptCount.merge(deptId, 1L, Long::sum);

                                // Lấy tên department nếu chưa có
                                if (!deptNames.containsKey(deptId)) {
                                        JsonNode dept = userServiceClient.getDepartmentById(token, deptId);
                                        if (dept != null && dept.has("name")) {
                                                deptNames.put(deptId, dept.get("name").asText());
                                        } else {
                                                deptNames.put(deptId, "Phòng ban #" + deptId);
                                        }
                                }
                        }
                }

                Long total = (long) applications.size();
                List<ApplicationChartDTO.DepartmentDataItem> departmentData = new ArrayList<>();
                int colorIndex = 0;

                for (Map.Entry<Long, Long> entry : deptCount.entrySet()) {
                        Long deptId = entry.getKey();
                        Long count = entry.getValue();
                        Double percentage = total > 0 ? (count.doubleValue() / total.doubleValue()) * 100 : 0.0;

                        departmentData.add(ApplicationChartDTO.DepartmentDataItem.builder()
                                        .departmentId(deptId)
                                        .departmentName(deptNames.getOrDefault(deptId, "Phòng ban #" + deptId))
                                        .count(count)
                                        .percentage(Math.round(percentage * 100.0) / 100.0)
                                        .color(colors[colorIndex % colors.length])
                                        .build());
                        colorIndex++;
                }

                // Sắp xếp giảm dần theo count
                departmentData.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));

                return departmentData;
        }

        /**
         * Tính toán dữ liệu position chart
         */
        private List<ApplicationChartDTO.PositionDataItem> calculatePositionChartData(List<JsonNode> applications,
                        String token) {
                Map<Long, Long> positionCount = new HashMap<>();
                Map<Long, String> positionNames = new HashMap<>();
                String[] colors = { "#8B5CF6", "#EC4899", "#10B981", "#3B82F6", "#F59E0B", "#EF4444", "#6366F1" };

                // Đếm theo position
                for (JsonNode app : applications) {
                        if (app.has("jobPositionId") && !app.get("jobPositionId").isNull()) {
                                Long positionId = app.get("jobPositionId").asLong();
                                positionCount.merge(positionId, 1L, Long::sum);

                                // Lấy tên position nếu chưa có
                                if (!positionNames.containsKey(positionId)) {
                                        try {
                                                PaginationDTO positions = jobServiceClient.getJobPositions(token, 1,
                                                                1000);
                                                if (positions != null && positions.getResult() != null) {
                                                        @SuppressWarnings("unchecked")
                                                        List<Object> positionList = (List<Object>) positions
                                                                        .getResult();
                                                        for (Object pos : positionList) {
                                                                JsonNode posNode = convertToJsonNode(pos);
                                                                if (posNode != null && posNode.has("id")
                                                                                && posNode.get("id")
                                                                                                .asLong() == positionId) {
                                                                        if (posNode.has("title")) {
                                                                                positionNames.put(positionId,
                                                                                                posNode.get("title")
                                                                                                                .asText());
                                                                        }
                                                                        break;
                                                                }
                                                        }
                                                }
                                        } catch (Exception e) {
                                                log.warn("Error fetching position name: {}", e.getMessage());
                                        }
                                        if (!positionNames.containsKey(positionId)) {
                                                positionNames.put(positionId, "Vị trí #" + positionId);
                                        }
                                }
                        }
                }

                Long total = (long) applications.size();
                List<ApplicationChartDTO.PositionDataItem> positionData = new ArrayList<>();
                int colorIndex = 0;

                for (Map.Entry<Long, Long> entry : positionCount.entrySet()) {
                        Long positionId = entry.getKey();
                        Long count = entry.getValue();
                        Double percentage = total > 0 ? (count.doubleValue() / total.doubleValue()) * 100 : 0.0;

                        positionData.add(ApplicationChartDTO.PositionDataItem.builder()
                                        .positionId(positionId)
                                        .positionName(positionNames.getOrDefault(positionId, "Vị trí #" + positionId))
                                        .count(count)
                                        .percentage(Math.round(percentage * 100.0) / 100.0)
                                        .color(colors[colorIndex % colors.length])
                                        .build());
                        colorIndex++;
                }

                // Sắp xếp giảm dần theo count và lấy top 10
                positionData.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
                if (positionData.size() > 10) {
                        positionData = positionData.subList(0, 10);
                }

                return positionData;
        }
}
