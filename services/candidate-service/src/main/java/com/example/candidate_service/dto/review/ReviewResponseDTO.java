package com.example.candidate_service.dto.review;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewResponseDTO {
    private Long id;
    private Long applicationId;
    private Long reviewerId;
    private String reviewerName;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
