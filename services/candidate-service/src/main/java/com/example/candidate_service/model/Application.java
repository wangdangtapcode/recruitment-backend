package com.example.candidate_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

import com.example.candidate_service.utils.enums.ApplicationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "applications")
@Getter
@Setter
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate appliedDate;
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    private String priority;
    @Column(columnDefinition = "TEXT")
    private String rejectionReason;
    private String resumeUrl;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(nullable = true)
    private Long createdBy; // Lưu employeeId (người tạo đơn ứng tuyển)
    @Column(nullable = true)
    private Long updatedBy; // Lưu employeeId (người cập nhật đơn ứng tuyển)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = true)
    @JsonIgnore
    private Candidate candidate;

    private Long jobPositionId;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Comment> comments;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL)
    private Set<Review> reviews;

}