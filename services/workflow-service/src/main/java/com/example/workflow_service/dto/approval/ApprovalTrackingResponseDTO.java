package com.example.workflow_service.dto.approval;

import com.example.workflow_service.utils.enums.ApprovalStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class ApprovalTrackingResponseDTO {

    private Long id;

    private Long requestId;

    private Long stepId;

    private ApprovalStatus status;

    private Long assignedUserId;

    private Long actionUserId;

    private OffsetDateTime actionAt;

    private String notes;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
