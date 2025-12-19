package com.example.statistics_service.service;

import com.example.statistics_service.dto.Meta;
import com.example.statistics_service.dto.PaginationDTO;
import com.example.statistics_service.dto.dashboard.*;
import com.example.statistics_service.dto.statistics.*;
import com.example.statistics_service.service.client.*;
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
        private final WorkflowServiceClient workflowServiceClient;
        private final CommunicationServiceClient communicationServiceClient;
        private final StatisticsCalculationService statisticsCalculationService;
        private final ObjectMapper objectMapper = new ObjectMapper();

        /**
         * Lấy dashboard cho ADMIN - Tổng quan toàn hệ thống
         */
        @Cacheable(value = "dashboard-stats", key = "'admin-' + #token")
        public AdminDashboardDTO getAdminDashboard(String token) {
                log.info("Fetching admin dashboard data");

                // Lấy dữ liệu từ các service
                PaginationDTO users = userServiceClient.getUsers(token, null, null, 1, 1);
                PaginationDTO departments = userServiceClient.getDepartments(token, 1, 100);
                PaginationDTO jobPositions = jobServiceClient.getJobPositions(token, 1, 1);
                PaginationDTO candidates = candidateServiceClient.getCandidates(token, 1, 1);
                PaginationDTO recruitmentRequests = jobServiceClient.getRecruitmentRequests(token, null, 1, 1);
                PaginationDTO workflows = workflowServiceClient.getWorkflows(token, true, 1, 1);
                PaginationDTO approvals = workflowServiceClient.getApprovalTrackings(token, null, 1, 1);

                // Tổng hợp dữ liệu
                AdminDashboardDTO.SystemOverviewDTO systemOverview = AdminDashboardDTO.SystemOverviewDTO.builder()
                                .totalUsers(getTotalFromPagination(users))
                                .totalDepartments(getTotalFromPagination(departments))
                                .totalJobPositions(getTotalFromPagination(jobPositions))
                                .totalCandidates(getTotalFromPagination(candidates))
                                .totalRecruitmentRequests(getTotalFromPagination(recruitmentRequests))
                                .totalApprovals(getTotalFromPagination(approvals))
                                .build();

                // Thống kê người dùng theo role
                Map<String, Long> usersByRole = new HashMap<>();
                usersByRole.put("ADMIN",
                                getTotalFromPagination(userServiceClient.getUsers(token, "ADMIN", true, 1, 1)));
                usersByRole.put("CEO", getTotalFromPagination(userServiceClient.getUsers(token, "CEO", true, 1, 1)));
                usersByRole.put("MANAGER",
                                getTotalFromPagination(userServiceClient.getUsers(token, "MANAGER", true, 1, 1)));
                usersByRole.put("STAFF",
                                getTotalFromPagination(userServiceClient.getUsers(token, "STAFF", true, 1, 1)));

                AdminDashboardDTO.UserStatisticsDTO userStatistics = AdminDashboardDTO.UserStatisticsDTO.builder()
                                .totalUsers(getTotalFromPagination(users))
                                .activeUsers(getTotalFromPagination(
                                                userServiceClient.getUsers(token, null, true, 1, 1)))
                                .inactiveUsers(getTotalFromPagination(
                                                userServiceClient.getUsers(token, null, false, 1, 1)))
                                .usersByRole(usersByRole)
                                .usersByDepartment(new HashMap<>()) // Có thể tính toán từ departments
                                .build();

                // Thống kê tuyển dụng
                PaginationDTO pendingRequests = jobServiceClient.getRecruitmentRequests(token, "PENDING", 1, 1);
                PaginationDTO approvedRequests = jobServiceClient.getRecruitmentRequests(token, "APPROVED", 1, 1);
                PaginationDTO rejectedRequests = jobServiceClient.getRecruitmentRequests(token, "REJECTED", 1, 1);

                AdminDashboardDTO.RecruitmentStatisticsDTO recruitmentStatistics = AdminDashboardDTO.RecruitmentStatisticsDTO
                                .builder()
                                .totalRecruitmentRequests(getTotalFromPagination(recruitmentRequests))
                                .pendingRequests(getTotalFromPagination(pendingRequests))
                                .approvedRequests(getTotalFromPagination(approvedRequests))
                                .rejectedRequests(getTotalFromPagination(rejectedRequests))
                                .totalApplications(
                                                getTotalFromPagination(candidateServiceClient.getApplications(token,
                                                                null, null, null, 1, 1)))
                                .pendingApplications(
                                                getTotalFromPagination(candidateServiceClient.getApplications(token,
                                                                "PENDING", null, null, 1, 1)))
                                .acceptedApplications(
                                                getTotalFromPagination(candidateServiceClient.getApplications(token,
                                                                "ACCEPTED", null, null, 1, 1)))
                                .rejectedApplications(
                                                getTotalFromPagination(candidateServiceClient.getApplications(token,
                                                                "REJECTED", null, null, 1, 1)))
                                .build();

                // Thống kê workflow
                PaginationDTO pendingApprovals = workflowServiceClient.getApprovalTrackings(token, "PENDING", 1, 1);
                PaginationDTO approvedApprovals = workflowServiceClient.getApprovalTrackings(token, "APPROVED", 1, 1);
                PaginationDTO rejectedApprovals = workflowServiceClient.getApprovalTrackings(token, "REJECTED", 1, 1);

                AdminDashboardDTO.WorkflowStatisticsDTO workflowStatistics = AdminDashboardDTO.WorkflowStatisticsDTO
                                .builder()
                                .totalWorkflows(getTotalFromPagination(workflows))
                                .activeWorkflows(getTotalFromPagination(
                                                workflowServiceClient.getWorkflows(token, true, 1, 1)))
                                .totalApprovals(getTotalFromPagination(approvals))
                                .pendingApprovals(getTotalFromPagination(pendingApprovals))
                                .approvedApprovals(getTotalFromPagination(approvedApprovals))
                                .rejectedApprovals(getTotalFromPagination(rejectedApprovals))
                                .build();

                // Thống kê theo phòng ban (đơn giản hóa)
                List<AdminDashboardDTO.DepartmentStatisticsDTO> departmentStatistics = new ArrayList<>();

                // Chart data (đơn giản hóa)
                AdminDashboardDTO.ChartDataDTO chartData = AdminDashboardDTO.ChartDataDTO.builder()
                                .recruitmentTrend(new ArrayList<>())
                                .applicationStatus(new ArrayList<>())
                                .departmentDistribution(new ArrayList<>())
                                .approvalStatus(new ArrayList<>())
                                .build();

                return AdminDashboardDTO.builder()
                                .systemOverview(systemOverview)
                                .userStatistics(userStatistics)
                                .recruitmentStatistics(recruitmentStatistics)
                                .workflowStatistics(workflowStatistics)
                                .departmentStatistics(departmentStatistics)
                                .chartData(chartData)
                                .build();
        }

        /**
         * Lấy dashboard cho CEO - Tổng quan công ty và phê duyệt
         */
        @Cacheable(value = "dashboard-stats", key = "'ceo-' + #token")
        public CEODashboardDTO getCEODashboard(String token) {
                log.info("Fetching CEO dashboard data");

                // Tổng quan công ty
                PaginationDTO employees = userServiceClient.getUsers(token, null, true, 1, 1);
                PaginationDTO departments = userServiceClient.getDepartments(token, 1, 1);
                PaginationDTO activeRequests = jobServiceClient.getRecruitmentRequests(token, "APPROVED", 1, 1);
                PaginationDTO candidates = candidateServiceClient.getCandidates(token, 1, 1);

                CEODashboardDTO.CompanyOverviewDTO companyOverview = CEODashboardDTO.CompanyOverviewDTO.builder()
                                .totalEmployees(getTotalFromPagination(employees))
                                .totalDepartments(getTotalFromPagination(departments))
                                .activeRecruitmentRequests(getTotalFromPagination(activeRequests))
                                .totalCandidates(getTotalFromPagination(candidates))
                                .build();

                // Tổng quan tuyển dụng
                PaginationDTO allRequests = jobServiceClient.getRecruitmentRequests(token, null, 1, 1);
                PaginationDTO pendingRequests = jobServiceClient.getRecruitmentRequests(token, "PENDING", 1, 1);
                PaginationDTO approvedRequests = jobServiceClient.getRecruitmentRequests(token, "APPROVED", 1, 1);

                CEODashboardDTO.RecruitmentOverviewDTO recruitmentOverview = CEODashboardDTO.RecruitmentOverviewDTO
                                .builder()
                                .totalRequests(getTotalFromPagination(allRequests))
                                .pendingRequests(getTotalFromPagination(pendingRequests))
                                .approvedRequests(getTotalFromPagination(approvedRequests))
                                .highPriorityRequests(0L) // Cần logic tính toán
                                .totalApplications(
                                                getTotalFromPagination(candidateServiceClient.getApplications(token,
                                                                null, null, null, 1, 1)))
                                .acceptedApplications(
                                                getTotalFromPagination(candidateServiceClient.getApplications(token,
                                                                "ACCEPTED", null, null, 1, 1)))
                                .build();

                // Tổng quan phê duyệt
                PaginationDTO pendingApprovals = workflowServiceClient.getApprovalTrackings(token, "PENDING", 1, 1);

                CEODashboardDTO.ApprovalOverviewDTO approvalOverview = CEODashboardDTO.ApprovalOverviewDTO.builder()
                                .totalPendingApprovals(getTotalFromPagination(pendingApprovals))
                                .totalApprovedThisMonth(0L) // Cần logic tính toán theo tháng
                                .totalRejectedThisMonth(0L)
                                .averageApprovalTime(0L)
                                .approvalTrend(new ArrayList<>())
                                .build();

                // Yêu cầu chờ phê duyệt của CEO
                List<CEODashboardDTO.PendingApprovalDTO> pendingCEOApprovals = new ArrayList<>();

                return CEODashboardDTO.builder()
                                .companyOverview(companyOverview)
                                .recruitmentOverview(recruitmentOverview)
                                .approvalOverview(approvalOverview)
                                .departmentOverviews(new ArrayList<>())
                                .pendingCEOApprovals(pendingCEOApprovals)
                                .build();
        }

        /**
         * Lấy dashboard cho MANAGER - Tổng quan phòng ban
         */
        @Cacheable(value = "dashboard-stats", key = "'manager-' + #token + '-' + #departmentId")
        public ManagerDashboardDTO getManagerDashboard(String token, Long departmentId) {
                log.info("Fetching manager dashboard data for department: {}", departmentId);

                // Tổng quan phòng ban
                JsonNode department = userServiceClient.getDepartmentById(token, departmentId);
                String departmentName = department != null && department.has("name")
                                ? department.get("name").asText()
                                : "Unknown";

                PaginationDTO deptEmployees = userServiceClient.getUsers(token, null, true, 1, 1);
                PaginationDTO deptRequests = jobServiceClient.getRecruitmentRequests(token, null, 1, 1);
                PaginationDTO deptApplications = candidateServiceClient.getApplications(token, null, departmentId, null,
                                1, 1);

                ManagerDashboardDTO.DepartmentOverviewDTO departmentOverview = ManagerDashboardDTO.DepartmentOverviewDTO
                                .builder()
                                .departmentId(departmentId)
                                .departmentName(departmentName)
                                .totalEmployees(getTotalFromPagination(deptEmployees))
                                .activeRecruitmentRequests(getTotalFromPagination(deptRequests))
                                .totalApplications(getTotalFromPagination(deptApplications))
                                .build();

                // Thống kê tuyển dụng phòng ban
                PaginationDTO pendingRequests = jobServiceClient.getRecruitmentRequests(token, "PENDING", 1, 1);
                PaginationDTO approvedRequests = jobServiceClient.getRecruitmentRequests(token, "APPROVED", 1, 1);
                PaginationDTO rejectedRequests = jobServiceClient.getRecruitmentRequests(token, "REJECTED", 1, 1);

                ManagerDashboardDTO.DepartmentRecruitmentDTO departmentRecruitment = ManagerDashboardDTO.DepartmentRecruitmentDTO
                                .builder()
                                .totalRequests(getTotalFromPagination(deptRequests))
                                .pendingRequests(getTotalFromPagination(pendingRequests))
                                .approvedRequests(getTotalFromPagination(approvedRequests))
                                .rejectedRequests(getTotalFromPagination(rejectedRequests))
                                .totalApplications(getTotalFromPagination(deptApplications))
                                .pendingApplications(getTotalFromPagination(
                                                candidateServiceClient.getApplications(token, "PENDING", departmentId,
                                                                null, 1, 1)))
                                .acceptedApplications(getTotalFromPagination(
                                                candidateServiceClient.getApplications(token, "ACCEPTED", departmentId,
                                                                null, 1, 1)))
                                .rejectedApplications(getTotalFromPagination(
                                                candidateServiceClient.getApplications(token, "REJECTED", departmentId,
                                                                null, 1, 1)))
                                .recruitmentTrend(new ArrayList<>())
                                .build();

                // Thống kê ứng viên
                PaginationDTO candidates = candidateServiceClient.getCandidates(token, 1, 1);
                Map<String, Long> applicationsByStatus = new HashMap<>();
                applicationsByStatus.put("PENDING",
                                getTotalFromPagination(candidateServiceClient.getApplications(token, "PENDING",
                                                departmentId, null, 1, 1)));
                applicationsByStatus.put("ACCEPTED",
                                getTotalFromPagination(candidateServiceClient.getApplications(token, "ACCEPTED",
                                                departmentId, null, 1, 1)));
                applicationsByStatus.put("REJECTED",
                                getTotalFromPagination(candidateServiceClient.getApplications(token, "REJECTED",
                                                departmentId, null, 1, 1)));

                ManagerDashboardDTO.CandidateStatisticsDTO candidateStatistics = ManagerDashboardDTO.CandidateStatisticsDTO
                                .builder()
                                .totalCandidates(getTotalFromPagination(candidates))
                                .newCandidatesThisMonth(0L)
                                .totalApplications(getTotalFromPagination(deptApplications))
                                .pendingApplications(applicationsByStatus.get("PENDING"))
                                .acceptedApplications(applicationsByStatus.get("ACCEPTED"))
                                .rejectedApplications(applicationsByStatus.get("REJECTED"))
                                .applicationsByStatus(applicationsByStatus)
                                .build();

                return ManagerDashboardDTO.builder()
                                .departmentOverview(departmentOverview)
                                .departmentRecruitment(departmentRecruitment)
                                .candidateStatistics(candidateStatistics)
                                .pendingApprovals(new ArrayList<>())
                                .recentCandidates(new ArrayList<>())
                                .build();
        }

        /**
         * Lấy dashboard cho STAFF - Công việc cá nhân
         */
        @Cacheable(value = "dashboard-stats", key = "'staff-' + #token + '-' + #userId")
        public StaffDashboardDTO getStaffDashboard(String token, Long userId) {
                log.info("Fetching staff dashboard data for user: {}", userId);

                // Thông tin cá nhân
                StaffDashboardDTO.PersonalInfoDTO personalInfo = StaffDashboardDTO.PersonalInfoDTO.builder()
                                .userId(userId)
                                .userName("Staff User") // Cần lấy từ user service
                                .email("staff@example.com")
                                .departmentName("Department") // Cần lấy từ user service
                                .positionName("Staff")
                                .role("STAFF")
                                .build();

                // Công việc đang xử lý
                PaginationDTO myApplications = candidateServiceClient.getApplications(token, null, null, null, 1, 1);
                PaginationDTO pendingApplications = candidateServiceClient.getApplications(token, "PENDING", null, null,
                                1, 1);
                PaginationDTO completedApplications = candidateServiceClient.getApplications(token, "ACCEPTED", null,
                                null, 1, 1);

                StaffDashboardDTO.WorkInProgressDTO workInProgress = StaffDashboardDTO.WorkInProgressDTO.builder()
                                .totalApplicationsAssigned(getTotalFromPagination(myApplications))
                                .pendingApplications(getTotalFromPagination(pendingApplications))
                                .completedApplications(getTotalFromPagination(completedApplications))
                                .totalInterviewsScheduled(0L)
                                .upcomingInterviews(0L)
                                .build();

                // Thống kê cá nhân
                StaffDashboardDTO.PersonalStatisticsDTO personalStatistics = StaffDashboardDTO.PersonalStatisticsDTO
                                .builder()
                                .totalApplicationsProcessed(getTotalFromPagination(myApplications))
                                .applicationsThisMonth(0L)
                                .totalInterviewsConducted(0L)
                                .interviewsThisMonth(0L)
                                .averageProcessingTime(0.0)
                                .build();

                return StaffDashboardDTO.builder()
                                .personalInfo(personalInfo)
                                .workInProgress(workInProgress)
                                .myCandidates(new ArrayList<>())
                                .upcomingInterviews(new ArrayList<>())
                                .personalStatistics(personalStatistics)
                                .build();
        }

        /**
         * Helper method để lấy total từ PaginationDTO
         */
        private Long getTotalFromPagination(PaginationDTO pagination) {
                if (pagination == null || pagination.getMeta() == null) {
                        return 0L;
                }
                return pagination.getMeta().getTotal();
        }

        // ===================================================================
        // CÁC API THỐNG KÊ CHO DASHBOARD FRONTEND
        // ===================================================================

        /**
         * Lấy thống kê tổng quan với so sánh tuần trước
         * GET /api/v1/statistics-service/statistics/summary
         */
        // @Cacheable(value = "dashboard-stats", key = "'summary-' + #token")
        public SummaryStatisticsDTO getSummaryStatistics(String token) {
                log.info("Fetching summary statistics");

                LocalDate today = LocalDate.now();
                LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1); // Thứ 2
                LocalDate weekEnd = weekStart.plusDays(6); // Chủ nhật
                LocalDate lastWeekStart = weekStart.minusWeeks(1);
                LocalDate lastWeekEnd = lastWeekStart.plusDays(6);

                // Lấy dữ liệu tuần này
                PaginationDTO currentApplications = candidateServiceClient.getApplications(token, null, null, null, 1,
                                1000);
                PaginationDTO currentHired = candidateServiceClient.getApplications(token, "HIRED", null, null, 1,
                                1000);
                PaginationDTO currentInterviews = getInterviewCount(token, weekStart, weekEnd);
                PaginationDTO currentRejected = candidateServiceClient.getApplications(token, "REJECTED", null, null, 1,
                                1000);

                // Lấy dữ liệu tuần trước (đơn giản hóa - lấy tất cả rồi filter)
                Long lastWeekApplications = getApplicationsCountByDateRange(token, lastWeekStart, lastWeekEnd);
                Long lastWeekHired = getApplicationsCountByStatusAndDateRange(token, "HIRED", lastWeekStart,
                                lastWeekEnd);
                Long lastWeekInterviews = getInterviewCountByDateRange(token, lastWeekStart, lastWeekEnd);
                Long lastWeekRejected = getApplicationsCountByStatusAndDateRange(token, "REJECTED", lastWeekStart,
                                lastWeekEnd);

                Long currentAppsCount = getTotalFromPagination(currentApplications);
                Long currentHiredCount = getTotalFromPagination(currentHired);
                Long currentInterviewsCount = getTotalFromPagination(currentInterviews);
                Long currentRejectedCount = getTotalFromPagination(currentRejected);

                return SummaryStatisticsDTO.builder()
                                .applications(calculateStatisticItem(currentAppsCount, lastWeekApplications))
                                .hired(calculateStatisticItem(currentHiredCount, lastWeekHired))
                                .interviews(calculateStatisticItem(currentInterviewsCount, lastWeekInterviews))
                                .rejected(calculateStatisticItem(currentRejectedCount, lastWeekRejected))
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
         * Lấy xu hướng ứng tuyển theo 7 ngày
         * GET /api/v1/statistics-service/statistics/application-trends
         */
        @Cacheable(value = "dashboard-stats", key = "'trends-' + #token")
        public ApplicationTrendDTO getApplicationTrends(String token, String period) {
                log.info("Fetching application trends for period: {}", period);

                LocalDate today = LocalDate.now();
                LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1); // Thứ 2

                String[] dayNames = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
                List<ApplicationTrendDTO.DailyTrendItem> dailyTrend = new ArrayList<>();

                for (int i = 0; i < 7; i++) {
                        LocalDate day = weekStart.plusDays(i);
                        Long count = getApplicationsCountByDate(token, day);

                        dailyTrend.add(ApplicationTrendDTO.DailyTrendItem.builder()
                                        .day(dayNames[i])
                                        .count(count)
                                        .date(day.format(DateTimeFormatter.ISO_DATE))
                                        .build());
                }

                return ApplicationTrendDTO.builder()
                                .period(period != null ? period : "7 ngày này")
                                .dailyTrend(dailyTrend)
                                .build();
        }

        /**
         * Lấy phân bổ ứng tuyển theo vị trí
         * GET /api/v1/statistics-service/statistics/applications-by-position
         */
        @Cacheable(value = "dashboard-stats", key = "'apps-by-position-' + #token")
        public ApplicationByPositionDTO getApplicationsByPosition(String token) {
                log.info("Fetching applications by position");

                PaginationDTO jobPositions = jobServiceClient.getJobPositions(token, 1, 100);
                if (jobPositions == null || jobPositions.getResult() == null) {
                        return ApplicationByPositionDTO.builder()
                                        .positions(new ArrayList<>())
                                        .build();
                }

                @SuppressWarnings("unchecked")
                List<Object> positions = (List<Object>) jobPositions.getResult();

                // Lấy tổng số applications
                PaginationDTO allApplications = candidateServiceClient.getApplications(token, null, null, null, 1, 1);
                Long totalApplications = getTotalFromPagination(allApplications);

                String[] colors = { "purple", "pink", "green", "blue", "orange", "yellow", "red" };

                List<ApplicationByPositionDTO.PositionStatistic> positionStats = new ArrayList<>();
                for (int i = 0; i < positions.size(); i++) {
                        JsonNode pos = convertToJsonNode(positions.get(i));
                        if (pos == null)
                                continue;
                        Long jobPositionId = pos.has("id") ? pos.get("id").asLong() : null;
                        String title = pos.has("title") ? pos.get("title").asText() : "";

                        Long appCount = jobPositionId != null
                                        ? getApplicationCountByJobPosition(token, jobPositionId)
                                        : 0L;

                        Double percentage = totalApplications > 0
                                        ? (appCount.doubleValue() / totalApplications.doubleValue()) * 100
                                        : 0.0;

                        if (appCount > 0) {
                                positionStats.add(ApplicationByPositionDTO.PositionStatistic.builder()
                                                .jobPositionId(jobPositionId)
                                                .jobTitle(title)
                                                .percentage(Math.round(percentage * 100.0) / 100.0) // Làm tròn 2 chữ số
                                                .applicationCount(appCount)
                                                .barColor(colors[i % colors.length])
                                                .build());
                        }
                }

                // Sắp xếp giảm dần và lấy top 10
                positionStats.sort((a, b) -> Long.compare(b.getApplicationCount(), a.getApplicationCount()));
                if (positionStats.size() > 10) {
                        positionStats = positionStats.subList(0, 10);
                }

                return ApplicationByPositionDTO.builder()
                                .positions(positionStats)
                                .build();
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
                                        .changeText("So với tuần trước")
                                        .isIncrease(null)
                                        .build();
                }

                Double changePercent = ((current.doubleValue() - previous.doubleValue()) / previous.doubleValue())
                                * 100;
                Boolean isIncrease = current > previous;

                return SummaryStatisticsDTO.StatisticItem.builder()
                                .value(current)
                                .changePercent(Math.round(changePercent * 100.0) / 100.0)
                                .changeText("So với tuần trước")
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

        private Long getApplicationsCountByDate(String token, LocalDate date) {
                // Đơn giản hóa - lấy tất cả rồi filter
                PaginationDTO apps = candidateServiceClient.getApplications(token, null, null, null, 1, 1000);
                if (apps == null || apps.getResult() == null)
                        return 0L;

                @SuppressWarnings("unchecked")
                List<Object> applications = (List<Object>) apps.getResult();
                return applications.stream()
                                .filter(app -> {
                                        try {
                                                JsonNode appNode = convertToJsonNode(app);
                                                if (appNode != null && appNode.has("appliedDate")) {
                                                        String appliedDateStr = appNode.get("appliedDate").asText();
                                                        LocalDate appliedDate = LocalDate.parse(appliedDateStr);
                                                        return appliedDate.equals(date);
                                                }
                                        } catch (Exception e) {
                                                log.warn("Error parsing application date: {}", e.getMessage());
                                        }
                                        return false;
                                })
                                .count();
        }

        private Long getApplicationsCountByDateRange(String token, LocalDate start, LocalDate end) {
                PaginationDTO apps = candidateServiceClient.getApplications(token, null, null, null, 1, 1000);
                if (apps == null || apps.getResult() == null)
                        return 0L;

                @SuppressWarnings("unchecked")
                List<Object> applications = (List<Object>) apps.getResult();
                return applications.stream()
                                .filter(app -> {
                                        try {
                                                JsonNode appNode = convertToJsonNode(app);
                                                if (appNode != null && appNode.has("appliedDate")) {
                                                        String appliedDateStr = appNode.get("appliedDate").asText();
                                                        LocalDate appliedDate = LocalDate.parse(appliedDateStr);
                                                        return !appliedDate.isBefore(start)
                                                                        && !appliedDate.isAfter(end);
                                                }
                                        } catch (Exception e) {
                                                log.warn("Error parsing application date: {}", e.getMessage());
                                        }
                                        return false;
                                })
                                .count();
        }

        private Long getApplicationsCountByStatusAndDateRange(String token, String status, LocalDate start,
                        LocalDate end) {
                PaginationDTO apps = candidateServiceClient.getApplications(token, status, null, null, 1, 1000);
                if (apps == null || apps.getResult() == null)
                        return 0L;

                @SuppressWarnings("unchecked")
                List<Object> applications = (List<Object>) apps.getResult();
                return applications.stream()
                                .filter(app -> {
                                        try {
                                                JsonNode appNode = convertToJsonNode(app);
                                                if (appNode != null && appNode.has("appliedDate")) {
                                                        String appliedDateStr = appNode.get("appliedDate").asText();
                                                        LocalDate appliedDate = LocalDate.parse(appliedDateStr);
                                                        return !appliedDate.isBefore(start)
                                                                        && !appliedDate.isAfter(end);
                                                }
                                        } catch (Exception e) {
                                                log.warn("Error parsing application date: {}", e.getMessage());
                                        }
                                        return false;
                                })
                                .count();
        }

        private PaginationDTO getInterviewCount(String token, LocalDate start, LocalDate end) {
                List<JsonNode> schedules = communicationServiceClient.getSchedules(token, start, end, null, null);
                Long count = (long) schedules.size();

                PaginationDTO result = new PaginationDTO();
                Meta meta = new Meta();
                meta.setTotal(count);
                meta.setPage(1);
                meta.setPageSize(schedules.size());
                meta.setPages(1);
                result.setMeta(meta);
                result.setResult(schedules);
                return result;
        }

        private Long getInterviewCountByDateRange(String token, LocalDate start, LocalDate end) {
                List<JsonNode> schedules = communicationServiceClient.getSchedules(token, start, end, null, null);
                return (long) schedules.size();
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

        // ===================================================================
        // CÁC API THỐNG KÊ MỚI VỚI MONGODB
        // ===================================================================

        /**
         * Lấy thống kê doanh thu theo tháng/quý/năm
         * GET /api/v1/statistics-service/statistics/revenue
         */
        public RevenueStatisticsDTO getRevenueStatistics(String token, String periodType, Integer year, Integer month,
                        Integer quarter) {
                log.info("Getting revenue statistics: periodType={}, year={}, month={}, quarter={}",
                                periodType, year, month, quarter);

                // Tính toán và lưu vào MongoDB
                com.example.statistics_service.model.mongodb.RevenueStatistics statistics = statisticsCalculationService
                                .calculateAndSaveRevenueStatistics(token, periodType, year, month, quarter);

                // Lấy thống kê kỳ trước để so sánh
                RevenueStatisticsDTO.RevenueComparisonDTO comparison = null;
                if ("MONTHLY".equals(periodType)) {
                        Integer prevMonth = month > 1 ? month - 1 : 12;
                        Integer prevYear = month > 1 ? year : year - 1;
                        // Lấy thống kê kỳ trước từ repository
                        com.example.statistics_service.repository.mongodb.RevenueStatisticsRepository repo = statisticsCalculationService
                                        .getRevenueStatisticsRepository();
                        Optional<com.example.statistics_service.model.mongodb.RevenueStatistics> prevStats = repo
                                        .findByPeriodTypeAndYearAndMonth(periodType, prevYear, prevMonth);
                        if (prevStats.isPresent()) {
                                BigDecimal prevRevenue = prevStats.get().getTotalRevenue();
                                BigDecimal revenueChange = statistics.getTotalRevenue().subtract(prevRevenue);
                                Double changePercent = prevRevenue.compareTo(BigDecimal.ZERO) > 0
                                                ? (revenueChange.divide(prevRevenue, 4, RoundingMode.HALF_UP)
                                                                .doubleValue()) * 100
                                                : 0.0;
                                comparison = RevenueStatisticsDTO.RevenueComparisonDTO.builder()
                                                .previousTotalRevenue(prevRevenue)
                                                .revenueChange(revenueChange)
                                                .revenueChangePercent(Math.round(changePercent * 100.0) / 100.0)
                                                .isIncrease(revenueChange.compareTo(BigDecimal.ZERO) > 0)
                                                .build();
                        }
                }

                return RevenueStatisticsDTO.builder()
                                .periodType(statistics.getPeriodType())
                                .year(statistics.getYear())
                                .month(statistics.getMonth())
                                .quarter(statistics.getQuarter())
                                .totalRevenue(statistics.getTotalRevenue())
                                .hiredCount(statistics.getHiredCount())
                                .averageRevenuePerEmployee(statistics.getAverageRevenuePerEmployee())
                                .revenueByDepartment(statistics.getRevenueByDepartment())
                                .revenueByPosition(statistics.getRevenueByPosition())
                                .periodStart(statistics.getPeriodStart())
                                .periodEnd(statistics.getPeriodEnd())
                                .comparison(comparison)
                                .build();
        }

        /**
         * Lấy thống kê tuyển dụng theo thời gian
         * GET /api/v1/statistics-service/statistics/recruitment
         */
        public RecruitmentStatisticsDTO getRecruitmentStatistics(String token, String periodType,
                        Integer year, Integer month, Integer quarter, Integer day) {
                log.info("Getting recruitment statistics: periodType={}, year={}, month={}, quarter={}, day={}",
                                periodType, year, month, quarter, day);

                com.example.statistics_service.model.mongodb.RecruitmentStatistics statistics = statisticsCalculationService
                                .calculateAndSaveRecruitmentStatistics(
                                                token, periodType, year, month, quarter, day);

                return RecruitmentStatisticsDTO.builder()
                                .periodType(statistics.getPeriodType())
                                .year(statistics.getYear())
                                .month(statistics.getMonth())
                                .quarter(statistics.getQuarter())
                                .day(statistics.getDay())
                                .totalRequests(statistics.getTotalRequests())
                                .requestsByStatus(statistics.getRequestsByStatus())
                                .totalJobPositions(statistics.getTotalJobPositions())
                                .totalApplications(statistics.getTotalApplications())
                                .applicationsByStatus(statistics.getApplicationsByStatus())
                                .hiredCount(statistics.getHiredCount())
                                .hireRate(statistics.getHireRate())
                                .requestsByDepartment(statistics.getRequestsByDepartment())
                                .periodStart(statistics.getPeriodStart())
                                .periodEnd(statistics.getPeriodEnd())
                                .build();
        }

        /**
         * Lấy thống kê ứng viên theo thời gian
         * GET /api/v1/statistics-service/statistics/applications
         */
        public ApplicationStatisticsDTO getApplicationStatistics(String token, String periodType,
                        Integer year, Integer month, Integer quarter, Integer day) {
                log.info("Getting application statistics: periodType={}, year={}, month={}, quarter={}, day={}",
                                periodType, year, month, quarter, day);

                com.example.statistics_service.model.mongodb.ApplicationStatistics statistics = statisticsCalculationService
                                .calculateAndSaveApplicationStatistics(
                                                token, periodType, year, month, quarter, day);

                return ApplicationStatisticsDTO.builder()
                                .periodType(statistics.getPeriodType())
                                .year(statistics.getYear())
                                .month(statistics.getMonth())
                                .quarter(statistics.getQuarter())
                                .day(statistics.getDay())
                                .totalCandidates(statistics.getTotalCandidates())
                                .totalApplications(statistics.getTotalApplications())
                                .applicationsByStatus(statistics.getApplicationsByStatus())
                                .candidatesByStage(statistics.getCandidatesByStage())
                                .applicationsByPosition(statistics.getApplicationsByPosition())
                                .applicationsByDepartment(statistics.getApplicationsByDepartment())
                                .conversionRate(statistics.getConversionRate())
                                .averageProcessingTime(statistics.getAverageProcessingTime())
                                .periodStart(statistics.getPeriodStart())
                                .periodEnd(statistics.getPeriodEnd())
                                .build();
        }
}
