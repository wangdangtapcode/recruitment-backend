package com.example.user_service.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.user_service.dto.PaginationDTO;
import com.example.user_service.dto.role.CreateRoleDTO;
import com.example.user_service.model.Role;
import com.example.user_service.service.RoleService;
import com.example.user_service.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/user-service/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @ApiMessage("Lấy danh sách tất cả vai trò")
    public ResponseEntity<PaginationDTO> getAll(
            @RequestParam(name = "isActive", required = false) Boolean isActive,
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
        return ResponseEntity.ok(roleService.getAllWithFilters(isActive, keyword, pageable));
    }

    @PostMapping
    @ApiMessage("Tạo mới vai trò")
    public ResponseEntity<Role> create(@RequestBody CreateRoleDTO createRoleDTO) {
        return ResponseEntity.ok(roleService.create(createRoleDTO));
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật vai trò")
    public ResponseEntity<Role> update(@PathVariable Long id, @RequestBody CreateRoleDTO createRoleDTO) {
        return ResponseEntity.ok(roleService.update(id, createRoleDTO));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xóa vai trò")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
