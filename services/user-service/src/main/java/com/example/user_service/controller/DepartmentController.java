package com.example.user_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping
    @ApiMessage("Lấy danh sách tất cả phòng ban")
    public ResponseEntity<List<Department>> getAll() {
        return ResponseEntity.ok(departmentService.getAll());
    }

    @GetMapping("/{id}")
    @ApiMessage("Lấy phòng ban theo id")
    public ResponseEntity<Department> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }
}
