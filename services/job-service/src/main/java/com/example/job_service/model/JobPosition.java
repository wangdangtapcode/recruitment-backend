package com.example.job_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.job_service.utils.enums.JobPositionStatus;

@Entity
@Table(name = "job_positions")
@Getter
@Setter
public class JobPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String responsibilities;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "preferred_qualifications", columnDefinition = "TEXT")
    private String preferredQualifications;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    private BigDecimal salaryRangeMin;
    private BigDecimal salaryRangeMax;
    private String currency;
    private String employmentType;
    private String experienceLevel;
    private String workLocation;
    private boolean remoteWorkAllowed;
    private int numberOfOpenings;
    private LocalDate applicationDeadline;
    private LocalDateTime publishedAt;
    private LocalDateTime closedAt;
    private int applicationCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobPositionStatus status;
    @OneToMany(mappedBy = "position", fetch = FetchType.LAZY)
    private List<JobPositionSkill> jobPositionSkills;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_request_id")
    private RecruitmentRequest recruitmentRequest;

}