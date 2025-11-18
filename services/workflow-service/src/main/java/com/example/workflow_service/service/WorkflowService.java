package com.example.workflow_service.service;

import com.example.workflow_service.dto.PaginationDTO;
import com.example.workflow_service.dto.workflow.CreateWorkflowDTO;
import com.example.workflow_service.dto.workflow.UpdateWorkflowDTO;
import com.example.workflow_service.dto.workflow.WorkflowResponseDTO;
import com.example.workflow_service.dto.workflow.WorkflowStepResponseDTO;
import com.example.workflow_service.exception.CustomException;
import com.example.workflow_service.exception.IdInvalidException;
import com.example.workflow_service.model.Workflow;
import com.example.workflow_service.model.WorkflowStep;
import com.example.workflow_service.repository.WorkflowRepository;
import com.example.workflow_service.repository.WorkflowStepRepository;
import com.example.workflow_service.utils.SecurityUtil;
import com.example.workflow_service.utils.enums.WorkflowType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final UserService userService;

    @Transactional
    public WorkflowResponseDTO create(CreateWorkflowDTO dto) {
        // Kiểm tra tên workflow đã tồn tại
        if (workflowRepository.findByName(dto.getName()).isPresent()) {
            throw new CustomException("Tên workflow đã tồn tại: " + dto.getName());
        }

        Workflow workflow = new Workflow();
        workflow.setName(dto.getName());
        workflow.setDescription(dto.getDescription());
        workflow.setType(dto.getType());
        workflow.setApplyConditions(dto.getApplyConditions());
        workflow.setIsActive(true);
        workflow.setCreatedBy(SecurityUtil.extractEmployeeId());

        final Workflow savedWorkflow = workflowRepository.save(workflow);

        // Tạo các steps nếu có
        if (dto.getSteps() != null && !dto.getSteps().isEmpty()) {
            List<WorkflowStep> steps = dto.getSteps().stream()
                    .map(stepDTO -> {
                        WorkflowStep step = new WorkflowStep();
                        step.setWorkflow(savedWorkflow);
                        step.setStepOrder(stepDTO.getStepOrder());
                        step.setStepName(stepDTO.getStepName());
                        step.setApproverPositionId(stepDTO.getApproverPositionId());
                        step.setIsActive(true);
                        return step;
                    })
                    .collect(Collectors.toList());
            workflowStepRepository.saveAll(steps);
        }

        return toResponseDTO(savedWorkflow);
    }

    @Transactional(readOnly = true)
    public WorkflowResponseDTO getById(Long id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy workflow với ID: " + id));
        return toResponseDTO(workflow);
    }

    @Transactional(readOnly = true)
    public PaginationDTO getAll(
            WorkflowType type,
            Boolean isActive,
            String keyword,
            Pageable pageable) {
        Page<Workflow> workflowPage = workflowRepository.findByFilters(
                type, isActive, keyword, pageable);

        List<Workflow> workflows = workflowPage.getContent();

        // Thu thập tất cả position IDs từ tất cả workflows
        Set<Long> allPositionIds = workflows.stream()
                .filter(w -> w.getSteps() != null)
                .flatMap(w -> w.getSteps().stream())
                .map(WorkflowStep::getApproverPositionId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // Lấy token từ SecurityContext
        String token = SecurityUtil.getCurrentUserJWT().orElse(null);
        // Gọi user-service một lần để lấy tất cả position names
        Map<Long, String> positionNamesMap = userService.getPositionNamesByIds(
                allPositionIds.stream().collect(Collectors.toList()),
                token);
        // Convert workflows sang DTOs với position names
        List<WorkflowResponseDTO> content = workflows.stream()
                .map(workflow -> toResponseDTO(workflow, positionNamesMap))
                .collect(Collectors.toList());

        PaginationDTO paginationDTO = new PaginationDTO();
        com.example.workflow_service.dto.Meta meta = new com.example.workflow_service.dto.Meta();
        meta.setPage(workflowPage.getNumber() + 1);
        meta.setPageSize(workflowPage.getSize());
        meta.setPages(workflowPage.getTotalPages());
        meta.setTotal(workflowPage.getTotalElements());
        paginationDTO.setMeta(meta);
        paginationDTO.setResult(content);

        return paginationDTO;
    }

    @Transactional
    public WorkflowResponseDTO update(Long id, UpdateWorkflowDTO dto) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy workflow với ID: " + id));

        // Kiểm tra tên workflow nếu thay đổi
        if (dto.getName() != null && !dto.getName().equals(workflow.getName())) {
            if (workflowRepository.findByName(dto.getName()).isPresent()) {
                throw new CustomException("Tên workflow đã tồn tại: " + dto.getName());
            }
            workflow.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            workflow.setDescription(dto.getDescription());
        }
        if (dto.getType() != null) {
            workflow.setType(dto.getType());
        }
        if (dto.getApplyConditions() != null) {
            workflow.setApplyConditions(dto.getApplyConditions());
        }
        if (dto.getIsActive() != null) {
            workflow.setIsActive(dto.getIsActive());
        }
        workflow.setUpdatedBy(SecurityUtil.extractEmployeeId());

        // Cập nhật steps nếu có
        if (dto.getSteps() != null) {
            // Xóa các steps cũ
            workflowStepRepository.deleteByWorkflowId(id);

            // Tạo các steps mới
            final Workflow finalWorkflow = workflow;
            if (!dto.getSteps().isEmpty()) {
                List<WorkflowStep> steps = dto.getSteps().stream()
                        .map(stepDTO -> {
                            WorkflowStep step = new WorkflowStep();
                            step.setWorkflow(finalWorkflow);
                            step.setStepOrder(stepDTO.getStepOrder());
                            step.setStepName(stepDTO.getStepName());
                            step.setApproverPositionId(stepDTO.getApproverPositionId());
                            step.setIsActive(true);
                            return step;
                        })
                        .collect(Collectors.toList());
                workflowStepRepository.saveAll(steps);
            }
        }

        workflow = workflowRepository.save(workflow);
        return toResponseDTO(workflow);
    }

    @Transactional
    public void delete(Long id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy workflow với ID: " + id));

        // Soft delete
        workflow.setIsActive(false);
        workflowRepository.save(workflow);
    }

    @Transactional(readOnly = true)
    public WorkflowResponseDTO findMatchingWorkflow(Long departmentId, Long levelId) {
        Workflow workflow = workflowRepository.findMatchingWorkflow(departmentId, levelId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy workflow phù hợp với department_id: " + departmentId +
                                " và level_id: " + levelId));
        return toResponseDTO(workflow);
    }

    private WorkflowResponseDTO toResponseDTO(Workflow workflow) {
        // Thu thập position IDs từ workflow này
        Set<Long> positionIds = workflow.getSteps() != null
                ? workflow.getSteps().stream()
                        .map(WorkflowStep::getApproverPositionId)
                        .filter(id -> id != null)
                        .collect(Collectors.toSet())
                : Set.of();

        // Lấy token từ SecurityContext
        String token = SecurityUtil.getCurrentUserJWT().orElse(null);

        // Gọi user-service để lấy position names
        Map<Long, String> positionNamesMap = userService.getPositionNamesByIds(
                positionIds.stream().collect(Collectors.toList()),
                token);

        return toResponseDTO(workflow, positionNamesMap);
    }

    private WorkflowResponseDTO toResponseDTO(Workflow workflow, Map<Long, String> positionNamesMap) {
        WorkflowResponseDTO dto = new WorkflowResponseDTO();
        dto.setId(workflow.getId());
        dto.setName(workflow.getName());
        dto.setDescription(workflow.getDescription());
        dto.setType(workflow.getType());
        dto.setApplyConditions(workflow.getApplyConditions());
        dto.setIsActive(workflow.getIsActive());
        dto.setCreatedBy(workflow.getCreatedBy());
        dto.setUpdatedBy(workflow.getUpdatedBy());
        dto.setCreatedAt(workflow.getCreatedAt());
        dto.setUpdatedAt(workflow.getUpdatedAt());

        if (workflow.getSteps() != null) {
            List<WorkflowStepResponseDTO> stepDTOs = workflow.getSteps().stream()
                    .map(step -> toStepResponseDTO(step, positionNamesMap))
                    .collect(Collectors.toList());
            dto.setSteps(stepDTOs);
        }

        return dto;
    }

    private WorkflowStepResponseDTO toStepResponseDTO(WorkflowStep step, Map<Long, String> positionNamesMap) {
        WorkflowStepResponseDTO dto = new WorkflowStepResponseDTO();
        dto.setId(step.getId());
        dto.setStepOrder(step.getStepOrder());
        dto.setStepName(step.getStepName());
        dto.setApproverPositionId(step.getApproverPositionId());
        System.out.println("step.getApproverPositionId(): " + step.getApproverPositionId());
        System.out.println("positionNamesMap: " + positionNamesMap);

        // Lấy position name từ map
        if (step.getApproverPositionId() != null) {
            String positionName = positionNamesMap.get(step.getApproverPositionId());
            dto.setApproverPositionName(positionName);
        }

        dto.setIsActive(step.getIsActive());
        dto.setCreatedAt(step.getCreatedAt());
        dto.setUpdatedAt(step.getUpdatedAt());
        return dto;
    }
}
