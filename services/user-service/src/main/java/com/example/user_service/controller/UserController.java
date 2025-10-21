package com.example.user_service.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.user_service.dto.PaginationDTO;
import com.example.user_service.dto.user.CreateUserDTO;
import com.example.user_service.exception.IdInvalidException;
import com.example.user_service.model.User;
import com.example.user_service.service.UserService;

@RestController
@RequestMapping("/api/v1/user-service/users")
public class UserController {
    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<PaginationDTO> findAll(
            @RequestParam("currentPage") Optional<String> currentPageOptional,
            @RequestParam("pageSize") Optional<String> pageSizeOptional) {

        String sCurrentPage = currentPageOptional.isPresent() ? currentPageOptional.get() : "";
        String sPageSize = pageSizeOptional.isPresent() ? pageSizeOptional.get() : "";

        int current = Integer.parseInt(sCurrentPage);
        int pageSize = Integer.parseInt(sPageSize);

        Pageable pageable = PageRequest.of(current - 1, pageSize);

        return ResponseEntity.status(HttpStatus.OK).body(this.userService.getAll(pageable));
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody CreateUserDTO u) {
        String hashPassword = this.passwordEncoder.encode(u.getPassword());
        u.setPassword(hashPassword);
        User user = this.userService.create(u);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (id >= 100) {
            throw new IdInvalidException("Id khong vuot qua 100");
        }
        this.userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        User user = this.userService.getById(id);
        return ResponseEntity.ok(user);
    }

}
