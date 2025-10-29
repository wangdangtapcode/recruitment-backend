package com.example.candidate_service.dto.application;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import com.example.candidate_service.utils.enums.ApplicationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateApplicationDTO {
    @NotNull(message = "Job Position ID không được để trống")
    private Long jobPositionId;

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
