package com.example.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.user_service.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    User findByRefreshTokenAndEmail(String token, String email);

    Optional<User> findById(Long id);
}
