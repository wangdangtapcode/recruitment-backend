package com.example.candidate_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import com.example.candidate_service.utils.enums.ApplicationStatus;

@Entity
@Table(name = "applications")
@Getter
@Setter
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate appliedDate;

    private ApplicationStatus status;
    private String priority;
    private String fullName;
    @Column(unique = true)
    private String email;

    private String phone;
    @Column(columnDefinition = "TEXT")
    private String rejectionReason;
    private String resumeUrl;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(nullable = true)
    private Long createdBy;
    @Column(nullable = true)
    private Long updatedBy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = true)
    private Candidate candidate;

    private Long jobPositionId;

}