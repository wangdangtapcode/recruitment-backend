package com.example.user_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Setter;

@Entity
@Table(name = "employees")
@Getter
@Setter
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mối quan hệ n-1: Nhiều Employee thuộc 1 Position
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_id")
    private Position position;

    @OneToOne(mappedBy = "employee", fetch = FetchType.EAGER)
    @JsonIgnore
    private User user;
    private String avatarUrl;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    private String name;

    private String phone;

    private String email;

    private String gender; // Có thể đổi thành Enum (MALE, FEMALE, OTHER)

    private String address;

    private String nationality;

    private LocalDate dateOfBirth;

    private String idNumber;

    private String status; // Có thể đổi thành Enum (ACTIVE, INACTIVE, ...)
}
