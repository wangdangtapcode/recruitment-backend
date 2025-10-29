package com.example.candidate_service.dto.application;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UploadCVDTO {
    @NotNull(message = "Job Position ID không được để trống")
    private Long jobPositionId;

    // Thông tin ứng viên (chưa có tài khoản)
    @NotNull(message = "Họ tên không được để trống")
    private String fullName;

    @NotNull(message = "Email không được để trống")
    private String email;

    @NotNull(message = "Số điện thoại không được để trống")
    private String phone;

    @NotNull(message = "CV file không được để trống")
    private MultipartFile cvFile;

    private String notes;
}
