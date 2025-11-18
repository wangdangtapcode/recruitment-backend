package com.example.job_service.controller;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.job_service.dto.PaginationDTO;
import com.example.job_service.dto.recruitment.ApproveRecruitmentRequestDTO;
import com.example.job_service.dto.recruitment.CreateRecruitmentRequestDTO;
import com.example.job_service.dto.recruitment.RecruitmentRequestWithUserDTO;
import com.example.job_service.exception.IdInvalidException;
import com.example.job_service.model.RecruitmentRequest;
import com.example.job_service.service.RecruitmentRequestService;
import com.example.job_service.utils.SecurityUtil;
import com.example.job_service.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/job-service/recruitment-requests")
public class RecruitmentRequestController {
    private final RecruitmentRequestService recruitmentRequestService;

    public RecruitmentRequestController(RecruitmentRequestService recruitmentRequestService) {
        this.recruitmentRequestService = recruitmentRequestService;
    }

    @PostMapping
    @ApiMessage("Tạo yêu cầu tuyển dụng")
    public ResponseEntity<RecruitmentRequest> create(@Validated @RequestBody CreateRecruitmentRequestDTO dto) {

        return ResponseEntity.ok(recruitmentRequestService.create(dto));
    }

    @PostMapping("/{id}/approve")
    @ApiMessage("Duyệt yêu cầu tuyển dụng")
    public ResponseEntity<RecruitmentRequest> approve(@PathVariable Long id,
            @Validated @RequestBody ApproveRecruitmentRequestDTO dto) throws IdInvalidException {

        Long approvedId = SecurityUtil.extractEmployeeId();
        return ResponseEntity.ok(recruitmentRequestService.approve(id, dto, approvedId));
    }

    @GetMapping("/department/{departmentId}")
    @ApiMessage("Lấy danh sách yêu cầu tuyển dụng theo phòng ban")
    public ResponseEntity<PaginationDTO> getAllByDepartmentId(
            @PathVariable Long departmentId,
            @RequestParam(name = "currentPage", defaultValue = "1", required = false) Optional<String> currentPageOptional,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) Optional<String> pageSizeOptional) {
        String sCurrentPage = currentPageOptional.orElse("1");
        String sPageSize = pageSizeOptional.orElse("10");

        int current = Integer.parseInt(sCurrentPage);
        int pageSize = Integer.parseInt(sPageSize);
        Pageable pageable = PageRequest.of(current - 1, pageSize);
        String token = SecurityUtil.getCurrentUserJWT().orElse(null);
        if (token == null) {
            throw new RuntimeException("Token không hợp lệ");
        }
        return ResponseEntity.ok(recruitmentRequestService.getAllByDepartmentIdWithUser(departmentId, token, pageable));
    }

    @GetMapping("/{id}")
    @ApiMessage("Lấy yêu cầu tuyển dụng theo id")
    public ResponseEntity<RecruitmentRequestWithUserDTO> getById(@PathVariable Long id) throws IdInvalidException {
        String token = SecurityUtil.getCurrentUserJWT().orElse(null);
        if (token == null) {
            throw new RuntimeException("Token không hợp lệ");
        }
        return ResponseEntity.ok(recruitmentRequestService.getByIdWithUser(id, token));
    }

    // Unified GET endpoint for all recruitment requests with filtering, pagination,
    // and sorting
    @GetMapping
    @ApiMessage("Lấy danh sách yêu cầu tuyển dụng với bộ lọc, phân trang và sắp xếp")
    public ResponseEntity<PaginationDTO> getAll(
            @RequestParam(name = "departmentId", required = false) Long departmentId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "createdBy", required = false) Long createdBy,
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

        String token = SecurityUtil.getCurrentUserJWT().orElse(null);
        if (token == null) {
            throw new RuntimeException("Token không hợp lệ");
        }

        return ResponseEntity.ok(recruitmentRequestService.getAllWithFilters(
                departmentId, status, createdBy, keyword, token, pageable));
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật yêu cầu tuyển dụng")
    public ResponseEntity<RecruitmentRequest> update(@PathVariable Long id,
            @Validated @RequestBody CreateRecruitmentRequestDTO dto) throws IdInvalidException {
        return ResponseEntity.ok(recruitmentRequestService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xóa yêu cầu tuyển dụng")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IdInvalidException {
        recruitmentRequestService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reject")
    @ApiMessage("Từ chối yêu cầu tuyển dụng")
    public ResponseEntity<RecruitmentRequest> reject(@PathVariable Long id,
            @RequestBody String reason) throws IdInvalidException {
        return ResponseEntity.ok(recruitmentRequestService.reject(id, reason));
    }

}
