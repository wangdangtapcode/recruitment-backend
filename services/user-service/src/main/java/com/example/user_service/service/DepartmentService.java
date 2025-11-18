package com.example.user_service.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.user_service.dto.Meta;
import com.example.user_service.dto.PaginationDTO;
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
                .orElse(null);
    }

    public PaginationDTO getAll(Pageable pageable) {
        Page<Department> pageDepartment = this.departmentRepository.findAll(pageable);
        PaginationDTO rs = new PaginationDTO();
        Meta mt = new Meta();
        mt.setPage(pageDepartment.getNumber() + 1);
        mt.setPageSize(pageDepartment.getSize());

        mt.setPages(pageDepartment.getTotalPages());
        mt.setTotal(pageDepartment.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(pageDepartment.getContent());
        return rs;
    }

    public PaginationDTO getAllWithFilters(Boolean isActive, String keyword, Pageable pageable) {
        Page<Department> pageDepartment = this.departmentRepository.findByFilters(isActive, keyword, pageable);
        PaginationDTO rs = new PaginationDTO();
        Meta mt = new Meta();
        mt.setPage(pageDepartment.getNumber() + 1);
        mt.setPageSize(pageDepartment.getSize());
        mt.setPages(pageDepartment.getTotalPages());
        mt.setTotal(pageDepartment.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(pageDepartment.getContent());
        return rs;
    }

    public List<Department> getByIds(List<Long> ids) {
        return departmentRepository.findAllById(ids);
    }

}
