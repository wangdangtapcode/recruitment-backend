package com.example.user_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.user_service.utils.SecurityUtil;
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

    private String gender;

    private String address;

    private String nationality;

    private LocalDate dateOfBirth;

    private String idNumber;

    private String status;

    // @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    private String createBy;
    private String updateBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updateBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.updatedAt = LocalDateTime.now();
    }
}
