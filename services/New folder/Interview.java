package com.example.communications_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    private String type; // "PHONE", "VIDEO", "IN_PERSON", "TECHNICAL", "HR"

    @Column(name = "status")
    private String status; // "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "RESCHEDULED"

    @Column(name = "location")
    private String location;

    private String link;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(name = "interviewer_id", nullable = false)
    private Long interviewerId;

    @Column(name = "job_position_id")
    private Long jobPositionId;

    @Column(name = "application_id")
    private Long applicationId;

    // Thêm fields để sync với các service khác
    @Column(name = "candidate_name")
    private String candidateName; // Cache từ Candidate Service

    @Column(name = "interviewer_name")
    private String interviewerName; // Cache từ User Service

    @Column(name = "job_title")
    private String jobTitle; // Cache từ Job Service

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "rating")
    private Integer rating; // 1-5 stars

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewParticipant> participants;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
