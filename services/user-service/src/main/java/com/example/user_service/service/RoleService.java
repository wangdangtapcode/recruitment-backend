package com.example.user_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.user_service.dto.role.CreateRoleDTO;
import com.example.user_service.model.Role;
import com.example.user_service.repository.RoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role getById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại"));
    }

    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    public Role create(CreateRoleDTO createRoleDTO) {
        Role role = new Role();
        role.setName(createRoleDTO.getName());
        role.setDescription(createRoleDTO.getDescription());
        return roleRepository.save(role);
    }

    public Role update(Long id, CreateRoleDTO createRoleDTO) {
        Role role = this.getById(id);
        role.setName(createRoleDTO.getName());
        role.setDescription(createRoleDTO.getDescription());
        return roleRepository.save(role);
    }

    public void delete(Long id) {
        roleRepository.deleteById(id);
    }
}
