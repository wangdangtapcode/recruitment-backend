package com.example.user_service.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.user_service.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
        User findByEmail(String email);

        User findByRefreshTokenAndEmail(String token, String email);

        Optional<User> findById(Long id);

        @Query("SELECT u FROM User u WHERE " +
                        "(:departmentId IS NULL OR u.department.id = :departmentId) AND " +
                        "(:role IS NULL OR u.role.name = :role) AND " +
                        "(:isActive IS NULL OR u.is_active = :isActive) AND " +
                        "(:keyword IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<User> findByFilters(@Param("departmentId") Long departmentId,
                        @Param("role") String role,
                        @Param("isActive") Boolean isActive,
                        @Param("keyword") String keyword,
                        Pageable pageable);
}
