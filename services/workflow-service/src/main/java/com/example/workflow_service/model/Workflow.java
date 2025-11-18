package com.example.workflow_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.workflow_service.utils.JpaJsonConverter;
import com.example.workflow_service.utils.enums.WorkflowType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "workflows")
@Getter
@Setter
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // tên luồng

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private WorkflowType type;

    @Column(name = "apply_conditions", columnDefinition = "JSON")
    @Convert(converter = JpaJsonConverter.class)
    private Map<String, Object> applyConditions;

    private Boolean isActive = true;

    private Long createdBy;

    private Long updatedBy;

    // Quan hệ với WorkflowStep
    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder asc")
    private List<WorkflowStep> steps;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
