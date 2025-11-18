package com.example.workflow_service.dto.workflow;

import com.example.workflow_service.utils.enums.WorkflowType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CreateWorkflowDTO {

    @NotBlank(message = "Tên workflow không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Loại workflow không được để trống")
    private WorkflowType type;

    // Điều kiện áp dụng workflow (JSON format: {"department_id": 1, "level_id": 3})
    private Map<String, Object> applyConditions;

    private Long createdBy;

    private List<CreateWorkflowStepDTO> steps;
}
