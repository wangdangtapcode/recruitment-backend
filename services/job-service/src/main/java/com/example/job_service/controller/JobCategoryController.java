package com.example.job_service.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.job_service.dto.PaginationDTO;
import com.example.job_service.model.JobCategory;
import com.example.job_service.service.JobCategoryService;
import com.example.job_service.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/job-service/job-categories")
public class JobCategoryController {
    private final JobCategoryService jobCategoryService;

    public JobCategoryController(JobCategoryService jobCategoryService) {
        this.jobCategoryService = jobCategoryService;
    }

    // Unified GET endpoint for all job categories with filtering, pagination, and
    // sorting
    @GetMapping
    @ApiMessage("Lấy danh sách danh mục công việc với bộ lọc, phân trang và sắp xếp")
    public ResponseEntity<PaginationDTO> getAll(
            @RequestParam(name = "departmentId", required = false) Long departmentId,
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

        return ResponseEntity.ok(jobCategoryService.getAllWithFilters(departmentId, keyword, pageable));
    }

    @GetMapping("/{departmentId}")
    @ApiMessage("Lấy danh mục theo phòng ban")
    public ResponseEntity<List<JobCategory>> getAllJobCategoriesByDepartmentId(@PathVariable Long departmentId) {
        List<JobCategory> jobCategories = jobCategoryService.getAllJobCategoriesByDepartmentId(departmentId);
        return ResponseEntity.ok(jobCategories);
    }
}
