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
public class CEODashboardDTO {
    // Tổng quan công ty
    private CompanyOverviewDTO companyOverview;

    // Thống kê tuyển dụng quan trọng
    private RecruitmentOverviewDTO recruitmentOverview;

    // Thống kê phê duyệt
    private ApprovalOverviewDTO approvalOverview;

    // Thống kê theo phòng ban
    private List<DepartmentOverviewDTO> departmentOverviews;

    // Yêu cầu chờ phê duyệt của CEO
    private List<PendingApprovalDTO> pendingCEOApprovals;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyOverviewDTO {
        private Long totalEmployees;
        private Long totalDepartments;
        private Long activeRecruitmentRequests;
        private Long totalCandidates;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecruitmentOverviewDTO {
        private Long totalRequests;
        private Long pendingRequests;
        private Long approvedRequests;
        private Long highPriorityRequests;
        private Long totalApplications;
        private Long acceptedApplications;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalOverviewDTO {
        private Long totalPendingApprovals;
        private Long totalApprovedThisMonth;
        private Long totalRejectedThisMonth;
        private Long averageApprovalTime; // giờ
        private List<Map<String, Object>> approvalTrend; // Theo thời gian
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentOverviewDTO {
        private Long departmentId;
        private String departmentName;
        private Long totalEmployees;
        private Long activeRecruitmentRequests;
        private Long totalApplications;
        private Long pendingApprovals;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingApprovalDTO {
        private Long approvalTrackingId;
        private String requestTitle;
        private String departmentName;
        private String requestType;
        private String priority;
        private String status;
        private String createdAt;
    }
}
