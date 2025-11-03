package com.example.candidate_service.dto.application;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateApplicationDTO {
    @NotNull(message = "Job Position ID không được để trống")
    private Long jobPositionId;

    // Khi tạo mới Application chỉ cần jobPositionId, cvFile, notes, createdBy. Phần
    // thông tin này lấy từ candidate.
    @NotNull(message = "Họ tên không được để trống")
    private String fullName;
    @NotNull(message = "Email không được để trống")
    private String email;
    @NotNull(message = "Số điện thoại không được để trống")
    private String phone;
    @NotNull(message = "CV file không được để trống")
    private MultipartFile cvFile;

    private String notes;
    private Long createdBy;
}
