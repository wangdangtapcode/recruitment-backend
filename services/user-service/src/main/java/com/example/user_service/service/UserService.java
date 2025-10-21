package com.example.user_service.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.user_service.dto.Meta;
import com.example.user_service.dto.PaginationDTO;
import com.example.user_service.dto.user.CreateUserDTO;
import com.example.user_service.model.Department;
import com.example.user_service.model.Role;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final DepartmentService departmentService;

    public UserService(UserRepository userRepository, RoleService roleService, DepartmentService departmentService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.departmentService = departmentService;
    }

    public PaginationDTO getAll(Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(pageable);
        PaginationDTO rs = new PaginationDTO();
        Meta mt = new Meta();
        mt.setPage(pageUser.getNumber());
        mt.setPageSize(pageUser.getSize());

        mt.setPages(pageUser.getTotalPages());
        mt.setTotal(pageUser.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(pageUser.getContent());
        return rs;
    }

    public User create(CreateUserDTO createUserDTO) {
        User user = new User();
        user.setName(createUserDTO.getName());
        user.setEmail(createUserDTO.getEmail());
        user.setPassword(createUserDTO.getPassword());
        user.setRole(this.roleService.getById(createUserDTO.getRoleId()));
        user.setDepartment(this.departmentService.getById(createUserDTO.getDepartmentId()));
        return this.userRepository.save(user);
    }

    public User update(User u) {
        return this.userRepository.save(u);
    }

    public void delete(Long id) {
        this.userRepository.deleteById(id);
        ;
    }

    public User handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public void updateUserRefreshToken(String token, String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshTokenAndEmail(String token, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(token, email);
    }

    public User getById(Long id) {
        return this.userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

}
