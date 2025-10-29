package com.example.communications_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "schedule")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; //

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    private String format;
    private String meetingType;
    @Column(name = "status")
    private String status; //

    private String location; //
    @Column(name = "start_time")
    private LocalDateTime startTime; //

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "timezone")
    private String timezone;

    private Integer reminderTime;
    @Column(name = "room_id")
    private Long roomId;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleParticipant> participants;

    private Long createdById;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}