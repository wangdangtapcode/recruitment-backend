package com.example.candidate_service.dto.application;

import org.springframework.web.multipart.MultipartFile;

import com.example.candidate_service.utils.enums.ApplicationStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateApplicationDTO {
    private ApplicationStatus status; // optional
    private String feedback; // optional
    private String rejectionReason; // optional
    private String notes; // optional
    private String priority; // optional
    private String resumeUrl; // optional (direct URL update)
    private MultipartFile cvFile; // optional (upload replaces resumeUrl)
    private Long updatedBy;
    private String fullName; // optional, dùng cập nhật Candidate
    private String email; // optional, dùng cập nhật Candidate
    private String phone; // optional, dùng cập nhật Candidate
}
