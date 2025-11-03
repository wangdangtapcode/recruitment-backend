package com.example.candidate_service.dto.candidate;

import java.math.BigDecimal;
import java.util.Set;

import com.example.candidate_service.model.Application;
import com.example.candidate_service.model.Candidate;
import com.example.candidate_service.model.Comment;
import com.example.candidate_service.utils.enums.CandidateStage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CandidateDetailResponseDTO {
    private Long id;
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
    private Set<Application> applications;
    private CandidateStage stage;

    public static CandidateDetailResponseDTO fromEntity(Candidate candidate) {
        CandidateDetailResponseDTO dto = new CandidateDetailResponseDTO();
        dto.setId(candidate.getId());
        dto.setFullName(candidate.getFullName());
        dto.setEmail(candidate.getEmail());
        dto.setPhone(candidate.getPhone());
        dto.setDateOfBirth(candidate.getDateOfBirth());
        dto.setGender(candidate.getGender());
        dto.setNationality(candidate.getNationality());
        dto.setIdNumber(candidate.getIdNumber());
        dto.setAddress(candidate.getAddress());
        dto.setAvatarUrl(candidate.getAvatarUrl());
        dto.setHighestEducation(candidate.getHighestEducation());
        dto.setUniversity(candidate.getUniversity());
        dto.setGraduationYear(candidate.getGraduationYear());
        dto.setGpa(candidate.getGpa());
        dto.setNotes(candidate.getNotes());
        dto.setStage(candidate.getStage());
        dto.setApplications(candidate.getApplications());
        return dto;
    }
}
