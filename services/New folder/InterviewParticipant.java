package com.example.communications_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_participants")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InterviewParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "participant_type", nullable = false)
    private String participantType; // "CANDIDATE", "INTERVIEWER", "OBSERVER"

    @Column(name = "participant_id", nullable = false)
    private Long participantId; // ID của User hoặc Candidate

    @Column(name = "response_status")
    private String responseStatus; // "PENDING", "ACCEPTED", "DECLINED", "TENTATIVE"

    @Column(name = "response_note", columnDefinition = "TEXT")
    private String responseNote;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @PrePersist
    public void prePersist() {
        if (this.responseStatus == null) {
            this.responseStatus = "PENDING";
        }
    }
}
