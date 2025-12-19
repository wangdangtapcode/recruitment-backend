package com.example.statistics_service.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    // Tổng quan hệ thống
    private SystemOverviewDTO systemOverview;

    // Thống kê người dùng
    private UserStatisticsDTO userStatistics;

    // Thống kê tuyển dụng
    private RecruitmentStatisticsDTO recruitmentStatistics;

    // Thống kê workflow
    private WorkflowStatisticsDTO workflowStatistics;

    // Thống kê theo phòng ban
    private List<DepartmentStatisticsDTO> departmentStatistics;

    // Biểu đồ dữ liệu
    private ChartDataDTO chartData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemOverviewDTO {
        private Long totalUsers;
        private Long totalDepartments;
        private Long totalJobPositions;
        private Long totalCandidates;
        private Long totalRecruitmentRequests;
        private Long totalApprovals;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatisticsDTO {
        private Long totalUsers;
        private Long activeUsers;
        private Long inactiveUsers;
        private Map<String, Long> usersByRole; // ADMIN, CEO, MANAGER, STAFF
        private Map<String, Long> usersByDepartment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecruitmentStatisticsDTO {
        private Long totalRecruitmentRequests;
        private Long pendingRequests;
        private Long approvedRequests;
        private Long rejectedRequests;
        private Long totalApplications;
        private Long pendingApplications;
        private Long acceptedApplications;
        private Long rejectedApplications;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowStatisticsDTO {
        private Long totalWorkflows;
        private Long activeWorkflows;
        private Long totalApprovals;
        private Long pendingApprovals;
        private Long approvedApprovals;
        private Long rejectedApprovals;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentStatisticsDTO {
        private Long departmentId;
        private String departmentName;
        private Long totalEmployees;
        private Long totalRecruitmentRequests;
        private Long totalApplications;
        private Long pendingApprovals;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartDataDTO {
        private List<Map<String, Object>> recruitmentTrend; // Theo thời gian
        private List<Map<String, Object>> applicationStatus; // Pie chart
        private List<Map<String, Object>> departmentDistribution; // Bar chart
        private List<Map<String, Object>> approvalStatus; // Pie chart
    }
}
