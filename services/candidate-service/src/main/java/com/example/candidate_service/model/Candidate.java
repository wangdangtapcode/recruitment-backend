package com.example.candidate_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Set;

@Entity
@Table(name = "candidates")
@Getter
@Setter
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private String dateOfBirth;

    private String gender;
    private String nationality;

    @Column(unique = true)
    private String idNumber;

    private String address;
    private String avatarUrl;
    private String resumeUrl;

    private String highestEducation;
    private String university;
    private String graduationYear;
    private BigDecimal gpa;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String languages;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_id")
    private Candidate referrer;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private Set<Application> applications;

}