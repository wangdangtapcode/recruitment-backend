package com.example.communications_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity
@Table(name = "notification_template")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name; // e.g., "Mời phỏng vấn", "Từ chối hồ sơ"

    @Column(name = "type")
    private String type; // e.g., "EMAIL", "IN_APP"

    @Column(name = "subject")
    private String subject; //

    // Lưu trữ các biến mẫu, ví dụ: { "candidate_name", "interview_date" }
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "variables", columnDefinition = "json")
    private Map<String, Object> variables;

    @Lob
    @Column(name = "content", columnDefinition = "TEXT")
    private String content; //

    @Column(name = "is_active")
    private Boolean isActive;
}