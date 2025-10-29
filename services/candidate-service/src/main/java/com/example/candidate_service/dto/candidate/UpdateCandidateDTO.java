package com.example.candidate_service.dto.candidate;

import java.math.BigDecimal;

import com.example.candidate_service.utils.enums.CandidateStage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCandidateDTO {
    private String fullName;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String gender;
    private String nationality;
    private String idNumber;
    private String address;
    private String avatarUrl;
    private String highestEducation;
    private String university;
    private String graduationYear;
    private BigDecimal gpa;
    private String notes;
    private CandidateStage stage;
}
