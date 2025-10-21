package com.example.user_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<Role>> getAll() {
        return ResponseEntity.ok(roleService.getAll());
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
