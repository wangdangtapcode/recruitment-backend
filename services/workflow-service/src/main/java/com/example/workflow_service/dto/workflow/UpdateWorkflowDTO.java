package com.example.workflow_service.dto.workflow;

import com.example.workflow_service.utils.enums.WorkflowType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UpdateWorkflowDTO {

    private String name;

    private String description;

    private WorkflowType type;

    private Map<String, Object> applyConditions;

    private Boolean isActive;

    private Long updatedBy;

    private List<CreateWorkflowStepDTO> steps;
}
