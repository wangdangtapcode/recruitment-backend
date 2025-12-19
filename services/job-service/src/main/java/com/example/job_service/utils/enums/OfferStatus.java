package com.example.job_service.utils.enums;

public enum OfferStatus {
    DRAFT, // Nháp, chưa submit
    PENDING, // Đang trong quá trình duyệt
    RETURNED, // Bị trả về để chỉnh sửa
    WITHDRAWN, // Đã rút lại
    APPROVED, // Đã được phê duyệt
    REJECTED, // Bị từ chối
    CANCELLED // Đã bị hủy
}
