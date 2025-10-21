package com.example.job_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "job_skills")
@Getter
@Setter
public class JobSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    private boolean isActive;

    @OneToMany(mappedBy = "skill", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<JobPositionSkill> jobPositionSkills;

}