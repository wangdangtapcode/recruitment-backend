package com.example.candidate_service.utils.enums;

public enum ApplicationStatus {
    SUBMITTED, // Đã nộp hồ sơ
    UNDER_REVIEW, // Đang xem xét
    SHORTLISTED, // Đã lọt vào vòng shortlist
    INTERVIEW_SCHEDULED, // Đã lên lịch phỏng vấn
    INTERVIEW_COMPLETED, // Đã hoàn thành phỏng vấn
    OFFERED, // Đã được đề nghị
    ACCEPTED, // Ứng viên chấp nhận
    REJECTED, // Bị từ chối
    WITHDRAWN, // Ứng viên rút lui
    HIRED, // Đã tuyển dụng
    ON_HOLD // Tạm dừng
}
