package com.example.candidate_service.dto.application;

import java.time.LocalDate;
import java.util.List;

import com.example.candidate_service.dto.comment.CommentResponseDTO;
import com.example.candidate_service.utils.enums.ApplicationStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationDetailResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate appliedDate;
    private ApplicationStatus status;
    private String priority;
    private String rejectionReason;
    private String resumeUrl;
    private String feedback;
    private String notes;
    private List<CommentResponseDTO> comments;
    private Object jobPosition; // hoặc Map tuỳ theo kết quả trả về từ job-service
    private List<Object> upcomingSchedules; // từ communications-service
}
