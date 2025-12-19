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
public class ManagerDashboardDTO {
    // Tổng quan phòng ban
    private DepartmentOverviewDTO departmentOverview;

    // Thống kê tuyển dụng của phòng ban
    private DepartmentRecruitmentDTO departmentRecruitment;

    // Thống kê ứng viên
    private CandidateStatisticsDTO candidateStatistics;

    // Yêu cầu chờ phê duyệt
    private List<PendingApprovalDTO> pendingApprovals;

    // Ứng viên mới nhất
    private List<RecentCandidateDTO> recentCandidates;

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
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentRecruitmentDTO {
        private Long totalRequests;
        private Long pendingRequests;
        private Long approvedRequests;
        private Long rejectedRequests;
        private Long totalApplications;
        private Long pendingApplications;
        private Long acceptedApplications;
        private Long rejectedApplications;
        private List<Map<String, Object>> recruitmentTrend; // Theo thời gian
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateStatisticsDTO {
        private Long totalCandidates;
        private Long newCandidatesThisMonth;
        private Long totalApplications;
        private Long pendingApplications;
        private Long acceptedApplications;
        private Long rejectedApplications;
        private Map<String, Long> applicationsByStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingApprovalDTO {
        private Long approvalTrackingId;
        private String requestTitle;
        private String requestType;
        private String priority;
        private String status;
        private String createdAt;
        private String currentStep;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentCandidateDTO {
        private Long candidateId;
        private String candidateName;
        private String position;
        private String status;
        private String appliedDate;
        private Double score; // Nếu có
    }
}
