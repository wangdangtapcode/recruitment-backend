package com.example.job_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.job_service.utils.enums.RecruitmentRequestStatus;

@Entity
@Table(name = "recruitment_requests")
@Getter
@Setter
public class RecruitmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private int numberOfPositions;
    private String priorityLevel;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String benefits;
    private boolean isExceedBudget;
    // Salary only if exceeds budget
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;

    private String location;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitmentRequestStatus status;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Column(columnDefinition = "TEXT")
    private String approvalNotes;
    private Long approvedId; // CEO Id
    private Long requesterId; // user request Id
    private LocalDateTime approvedAt;
    private boolean isActive;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_category_id", nullable = false)
    private JobCategory jobCategory;

    private Long departmentId;
}
