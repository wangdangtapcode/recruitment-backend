package com.example.job_service.dto.recruitment;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveRecruitmentRequestDTO {
    private String approvalNotes;
}
