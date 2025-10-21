package com.example.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.user_service.model.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

}
