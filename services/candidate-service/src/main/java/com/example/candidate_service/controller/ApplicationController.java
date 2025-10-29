package com.example.candidate_service.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.candidate_service.dto.application.ApplicationResponseDTO;
import com.example.candidate_service.dto.PaginationDTO;
import com.example.candidate_service.dto.application.CreateApplicationDTO;
import com.example.candidate_service.dto.application.UpdateApplicationDTO;
import com.example.candidate_service.exception.IdInvalidException;
import com.example.candidate_service.service.ApplicationService;
import com.example.candidate_service.utils.SecurityUtil;
import com.example.candidate_service.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/candidate-service/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    @ApiMessage("Lấy danh sách đơn ứng tuyển với bộ lọc, phân trang và sắp xếp")
    public ResponseEntity<PaginationDTO> getAllApplications(
            @RequestParam(name = "candidateId", required = false) Long candidateId,
            @RequestParam(name = "jobPositionId", required = false) Long jobPositionId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1", required = false) int page,
            @RequestParam(name = "limit", defaultValue = "10", required = false) int limit,
            @RequestParam(name = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = "desc", required = false) String sortOrder) {

        // Validate pagination parameters
        if (page < 1)
            page = 1;
        if (limit < 1 || limit > 100)
            limit = 10;

        // Create sort object
        Sort.Direction direction = sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);

        // Create pageable object
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        return ResponseEntity.ok(applicationService.getAllApplicationsWithFilters(
                candidateId, jobPositionId, status, keyword, pageable));
    }

    @PostMapping
    @ApiMessage("Tạo đơn ứng tuyển")
    public ResponseEntity<ApplicationResponseDTO> createApplication(@Validated @ModelAttribute CreateApplicationDTO dto)
            throws IOException, IdInvalidException {
        dto.setCreatedBy(SecurityUtil.extractUserId());
        return ResponseEntity.ok(applicationService.createApplication(dto));
    }

    @GetMapping("/{id}")
    @ApiMessage("Lấy thông tin đơn ứng tuyển")
    public ResponseEntity<ApplicationResponseDTO> getApplicationById(@PathVariable Long id) throws IdInvalidException {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @PutMapping("/{id}/status")
    @ApiMessage("Cập nhật trạng thái đơn ứng tuyển")
    public ResponseEntity<ApplicationResponseDTO> updateApplicationStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String feedback) throws IdInvalidException {
        return ResponseEntity
                .ok(applicationService.updateApplicationStatus(id, status, feedback, SecurityUtil.extractUserId()));
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật đơn ứng tuyển")
    public ResponseEntity<ApplicationResponseDTO> updateApplication(
            @PathVariable Long id,
            @Validated @ModelAttribute UpdateApplicationDTO dto) throws IdInvalidException, IOException {
        dto.setUpdatedBy(SecurityUtil.extractUserId());
        return ResponseEntity.ok(applicationService.updateApplication(id, dto));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xóa đơn ứng tuyển")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) throws IdInvalidException {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{applicationId}/accept")
    @ApiMessage("Chấp nhận đơn ứng tuyển")
    public ResponseEntity<ApplicationResponseDTO> acceptApplication(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String feedback) throws IdInvalidException {
        return ResponseEntity.ok(applicationService.acceptApplication(applicationId, feedback));
    }

    @PutMapping("/{applicationId}/reject")
    @ApiMessage("Từ chối đơn ứng tuyển")
    public ResponseEntity<ApplicationResponseDTO> rejectApplication(
            @PathVariable Long applicationId,
            @RequestParam String rejectionReason) throws IdInvalidException {
        return ResponseEntity.ok(applicationService.rejectApplication(applicationId, rejectionReason));
    }
}
