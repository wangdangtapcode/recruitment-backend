package com.example.job_service.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.job_service.dto.PaginationDTO;
import com.example.job_service.dto.jobposition.CreateJobPositionDTO;
import com.example.job_service.dto.jobposition.JobPositionResponseDTO;
import com.example.job_service.dto.jobposition.UpdateJobPositionDTO;
import com.example.job_service.exception.IdInvalidException;
import com.example.job_service.model.JobPosition;
import com.example.job_service.service.JobPositionService;
import com.example.job_service.utils.SecurityUtil;
import com.example.job_service.utils.annotation.ApiMessage;
import com.example.job_service.utils.enums.JobPositionStatus;

@RestController
@RequestMapping("/api/v1/job-service/job-positions")
public class JobPositionController {
    private final JobPositionService jobPositionService;

    public JobPositionController(JobPositionService jobPositionService) {
        this.jobPositionService = jobPositionService;
    }

    @PostMapping
    @ApiMessage("Tạo vị trí tuyển dụng từ yêu cầu đã duyệt")
    public ResponseEntity<JobPositionResponseDTO> create(@Validated @RequestBody CreateJobPositionDTO dto)
            throws IdInvalidException {
        JobPosition position = jobPositionService.create(dto);
        return ResponseEntity.ok(JobPositionResponseDTO.fromEntity(position));
    }

    @GetMapping("/{id}")
    @ApiMessage("Lấy thông tin vị trí tuyển dụng theo ID")
    public ResponseEntity<JobPositionResponseDTO> getById(@PathVariable Long id) throws IdInvalidException {
        String token = SecurityUtil.getCurrentUserJWT().orElse("");
        return ResponseEntity.ok(jobPositionService.getByIdWithDepartmentName(id, token));
    }

    // Unified GET endpoint for all job positions with filtering, pagination, and
    // sorting
    @GetMapping
    @ApiMessage("Lấy danh sách vị trí tuyển dụng với bộ lọc, phân trang và sắp xếp")
    public ResponseEntity<PaginationDTO> getAll(
            @RequestParam(name = "departmentId", required = false) Long departmentId,
            @RequestParam(name = "status", required = false) JobPositionStatus status,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "published", required = false) Boolean published,
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

        return ResponseEntity.ok(jobPositionService.findAllWithFilters(
                departmentId, status, categoryId, published, keyword, pageable, token));
    }

    @GetMapping("/status")
    @ApiMessage("Lấy danh sách vị trí tuyển dụng theo trạng thái")
    public ResponseEntity<List<JobPositionResponseDTO>> getByStatus(
            @RequestParam(name = "status", required = false) JobPositionStatus status) {
        List<JobPosition> positions = jobPositionService.findByStatus(status);
        List<JobPositionResponseDTO> response = positions.stream()
                .map(JobPositionResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/published")
    @ApiMessage("Lấy danh sách vị trí tuyển dụng đã xuất bản (phân trang)")
    public ResponseEntity<PaginationDTO> getPublishedPaged(
            @RequestParam(name = "departmentId", required = false) Long departmentId,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1", required = false) int page,
            @RequestParam(name = "limit", defaultValue = "10", required = false) int limit,
            @RequestParam(name = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = "desc", required = false) String sortOrder) {

        if (page < 1)
            page = 1;
        if (limit < 1 || limit > 100)
            limit = 10;

        Sort.Direction direction = sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        String token = SecurityUtil.getCurrentUserJWT().orElse(null);
        if (token == null) {
            throw new RuntimeException("Token không hợp lệ");
        }

        return ResponseEntity.ok(
                jobPositionService.findAllWithFilters(
                        departmentId,
                        JobPositionStatus.PUBLISHED,
                        categoryId,
                        true,
                        keyword,
                        pageable,
                        token));
    }

    @GetMapping("/published/{id}")
    @ApiMessage("Lấy thông tin vị trí tuyển dụng đã được xuất bản theo ID")
    public ResponseEntity<JobPositionResponseDTO> getPublishedById(@PathVariable Long id) throws IdInvalidException {
        return ResponseEntity.ok(jobPositionService.getByIdWithPublished(id));
    }

    @GetMapping("/department/{departmentId}")
    @ApiMessage("Lấy danh sách vị trí tuyển dụng theo phòng ban")
    public ResponseEntity<PaginationDTO> getByDepartmentId(@PathVariable Long departmentId,
            @RequestParam(name = "currentPage", defaultValue = "1", required = false) Optional<String> currentPageOptional,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) Optional<String> pageSizeOptional) {
        String sCurrentPage = currentPageOptional.orElse("1");
        String sPageSize = pageSizeOptional.orElse("10");

        int current = Integer.parseInt(sCurrentPage);
        int pageSize = Integer.parseInt(sPageSize);
        Pageable pageable = PageRequest.of(current - 1, pageSize);

        PaginationDTO positions = jobPositionService.findByDepartmentId(departmentId, pageable);
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/department/{departmentId}/status/{status}")
    @ApiMessage("Lấy danh sách vị trí tuyển dụng theo phòng ban và trạng thái")
    public ResponseEntity<List<JobPositionResponseDTO>> getByDepartmentIdAndStatus(
            @PathVariable Long departmentId,
            @PathVariable JobPositionStatus status) {
        List<JobPosition> positions = jobPositionService.findByDepartmentIdAndStatus(departmentId, status);
        List<JobPositionResponseDTO> response = positions.stream()
                .map(JobPositionResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật vị trí tuyển dụng")
    public ResponseEntity<JobPositionResponseDTO> update(@PathVariable Long id,
            @Validated @RequestBody UpdateJobPositionDTO dto) throws IdInvalidException {
        JobPosition position = jobPositionService.update(id, dto);
        return ResponseEntity.ok(JobPositionResponseDTO.fromEntity(position));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xóa vị trí tuyển dụng")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IdInvalidException {
        jobPositionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    @ApiMessage("Publish vị trí tuyển dụng")
    public ResponseEntity<JobPositionResponseDTO> publish(@PathVariable Long id) throws IdInvalidException {
        JobPosition position = jobPositionService.publish(id);
        return ResponseEntity.ok(JobPositionResponseDTO.fromEntity(position));
    }

    @PostMapping("/{id}/close")
    @ApiMessage("Đóng vị trí tuyển dụng")
    public ResponseEntity<JobPositionResponseDTO> close(@PathVariable Long id) throws IdInvalidException {
        JobPosition position = jobPositionService.close(id);
        return ResponseEntity.ok(JobPositionResponseDTO.fromEntity(position));
    }

    @PostMapping("/{id}/reopen")
    @ApiMessage("Mở lại vị trí tuyển dụng")
    public ResponseEntity<JobPositionResponseDTO> reopen(@PathVariable Long id) throws IdInvalidException {
        JobPosition position = jobPositionService.reopen(id);
        return ResponseEntity.ok(JobPositionResponseDTO.fromEntity(position));
    }

}
