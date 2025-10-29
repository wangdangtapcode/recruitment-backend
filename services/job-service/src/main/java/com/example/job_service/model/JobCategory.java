package com.example.job_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_categories")
@Getter
@Setter
public class JobCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    private boolean isActive;

    private Long departmentId;

}
