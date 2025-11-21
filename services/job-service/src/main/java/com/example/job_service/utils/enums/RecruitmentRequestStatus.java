package com.example.job_service.utils.enums;

public enum RecruitmentRequestStatus {
    DRAFT, // Nháp, chưa submit
    SUBMITTED, // Đã submit, đang chờ duyệt
    PENDING, // Đang trong quá trình duyệt (giữ lại để backward compatibility)
    RETURNED, // Bị trả về để chỉnh sửa
    WITHDRAWN, // Đã rút lại (submitter rút lại trước khi được xử lý)
    APPROVED, // Đã được phê duyệt
    REJECTED, // Bị từ chối
    CANCELLED, // Đã bị hủy
    COMPLETED // Hoàn thành (giữ lại để backward compatibility)
}
