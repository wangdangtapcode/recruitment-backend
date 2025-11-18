package com.example.workflow_service.dto.workflow;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class WorkflowStepResponseDTO {

    private Long id;

    private Integer stepOrder;

    private String stepName;

    private Long approverPositionId;

    private String approverPositionName;

    private Boolean isActive;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
