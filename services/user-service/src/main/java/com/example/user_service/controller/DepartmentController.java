package com.example.user_service.controller;

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

import com.example.user_service.dto.PaginationDTO;
import com.example.user_service.model.Department;
import com.example.user_service.service.DepartmentService;
import com.example.user_service.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/user-service/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // Unified GET endpoint for all departments with filtering, pagination, and
    // sorting
    @GetMapping
    @ApiMessage("Lấy danh sách phòng ban với bộ lọc, phân trang và sắp xếp")
    public ResponseEntity<PaginationDTO> getAll(
            @RequestParam(name = "isActive", required = false) Boolean isActive,
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

        return ResponseEntity.ok(departmentService.getAllWithFilters(isActive, keyword, pageable));
    }

    @GetMapping("/{id}")
    @ApiMessage("Lấy phòng ban theo id")
    public ResponseEntity<Department> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    @GetMapping(params = "ids")
    @ApiMessage("Lấy danh sách phòng ban theo nhiều IDs")
    public ResponseEntity<List<Department>> getByIds(@RequestParam("ids") String ids) {
        // Parse comma-separated IDs
        List<Long> departmentIds = List.of(ids.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();

        return ResponseEntity.ok(departmentService.getByIds(departmentIds));
    }
}
