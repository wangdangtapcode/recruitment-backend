package com.example.statistics_service.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDashboardDTO {
    // Thông tin cá nhân
    private PersonalInfoDTO personalInfo;

    // Công việc đang xử lý
    private WorkInProgressDTO workInProgress;

    // Ứng viên đang xử lý
    private List<MyCandidateDTO> myCandidates;

    // Lịch phỏng vấn
    private List<UpcomingInterviewDTO> upcomingInterviews;

    // Thống kê cá nhân
    private PersonalStatisticsDTO personalStatistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalInfoDTO {
        private Long userId;
        private String userName;
        private String email;
        private String departmentName;
        private String positionName;
        private String role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkInProgressDTO {
        private Long totalApplicationsAssigned;
        private Long pendingApplications;
        private Long completedApplications;
        private Long totalInterviewsScheduled;
        private Long upcomingInterviews;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyCandidateDTO {
        private Long candidateId;
        private String candidateName;
        private String position;
        private String status;
        private String assignedDate;
        private String lastUpdated;
        private String nextAction; // "Schedule Interview", "Review CV", etc.
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingInterviewDTO {
        private Long interviewId;
        private String candidateName;
        private String position;
        private String interviewDate;
        private String interviewTime;
        private String interviewType; // "Phone", "Video", "In-person"
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalStatisticsDTO {
        private Long totalApplicationsProcessed;
        private Long applicationsThisMonth;
        private Long totalInterviewsConducted;
        private Long interviewsThisMonth;
        private Double averageProcessingTime; // giờ
    }
}
