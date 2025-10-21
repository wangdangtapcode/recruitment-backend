package com.example.user_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.user_service.model.Department;
import com.example.user_service.repository.DepartmentRepository;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department không tồn tại"));
    }

    public List<Department> getAll() {
        return departmentRepository.findAll();
    }
    
}
