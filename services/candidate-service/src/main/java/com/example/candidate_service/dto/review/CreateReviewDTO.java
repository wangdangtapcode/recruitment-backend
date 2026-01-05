package com.example.candidate_service.dto.review;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReviewDTO {
    @NotNull(message = "Candidate ID là bắt buộc")
    private Long candidateId;

    private String comment; // Nhận xét đánh giá
}
