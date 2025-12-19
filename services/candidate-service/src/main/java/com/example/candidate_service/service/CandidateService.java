package com.example.candidate_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.candidate_service.repository.CandidateRepository;
import com.example.candidate_service.dto.Meta;
import com.example.candidate_service.dto.PaginationDTO;
import com.example.candidate_service.dto.candidate.CandidateGetAllResponseDTO;
import com.example.candidate_service.dto.candidate.CandidateDetailResponseDTO;
import com.example.candidate_service.dto.candidate.CreateCandidateDTO;
import com.example.candidate_service.dto.candidate.UpdateCandidateDTO;
import com.example.candidate_service.exception.IdInvalidException;
import com.example.candidate_service.model.Candidate;
import com.example.candidate_service.utils.enums.CandidateStage;

@Service
public class CandidateService {
    private final CandidateRepository candidateRepository;
    private final JobService jobService;

    public CandidateService(CandidateRepository candidateRepository, JobService jobService) {
        this.candidateRepository = candidateRepository;
        this.jobService = jobService;
    }

    public boolean existsByEmail(String email) {
        return candidateRepository.existsByEmail(email);
    }

    public Candidate findByEmail(String email) throws IdInvalidException {
        return candidateRepository.findByEmail(email)
                .orElseThrow(() -> new IdInvalidException("ứng viên không tồn tại"));
    }

    public Candidate saveCandidate(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    public PaginationDTO getAllWithFilters(CandidateStage stage, String keyword, Long departmentId, Pageable pageable) {
        List<Long> jobPositionIds = null;
        if (departmentId != null) {
            String token = com.example.candidate_service.utils.SecurityUtil.getCurrentUserJWT().orElse("");
            jobPositionIds = jobService.getJobPositionIdsByDepartmentId(departmentId, token);
            System.out.println("jobPositionIds: " + jobPositionIds);
            // If no job positions found for this department, return empty result
            if (jobPositionIds.isEmpty()) {
                PaginationDTO paginationDTO = new PaginationDTO();
                Meta meta = new Meta();
                meta.setPage(pageable.getPageNumber() + 1);
                meta.setPageSize(pageable.getPageSize());
                meta.setPages(0);
                meta.setTotal(0);
                paginationDTO.setMeta(meta);
                paginationDTO.setResult(List.of());
                return paginationDTO;
            }
        }

        Page<Candidate> pageData = candidateRepository.findByFilters(stage, keyword, jobPositionIds, pageable);

        PaginationDTO paginationDTO = new PaginationDTO();
        Meta meta = new Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageData.getTotalPages());
        meta.setTotal(pageData.getTotalElements());
        paginationDTO.setMeta(meta);
        paginationDTO.setResult(pageData.getContent().stream().map(CandidateGetAllResponseDTO::fromEntity).toList());
        return paginationDTO;
    }

    public CandidateDetailResponseDTO getById(Long id) throws IdInvalidException {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("ứng viên không tồn tại"));
        return CandidateDetailResponseDTO.fromEntity(candidate);
    }

    @Transactional
    public CandidateDetailResponseDTO create(CreateCandidateDTO dto) {
        Candidate candidate = new Candidate();
        candidate.setFullName(dto.getFullName());
        candidate.setEmail(dto.getEmail());
        candidate.setPhone(dto.getPhone());
        candidate.setDateOfBirth(dto.getDateOfBirth());
        candidate.setGender(dto.getGender());
        candidate.setNationality(dto.getNationality());
        candidate.setIdNumber(dto.getIdNumber());
        candidate.setAddress(dto.getAddress());
        candidate.setAvatarUrl(dto.getAvatarUrl());
        candidate.setHighestEducation(dto.getHighestEducation());
        candidate.setUniversity(dto.getUniversity());
        candidate.setGraduationYear(dto.getGraduationYear());
        candidate.setGpa(dto.getGpa());
        candidate.setNotes(dto.getNotes());
        candidate.setStage(dto.getStage());

        Candidate saved = candidateRepository.save(candidate);
        return CandidateDetailResponseDTO.fromEntity(saved);
    }

    @Transactional
    public CandidateDetailResponseDTO update(Long id, UpdateCandidateDTO dto) throws IdInvalidException {
        Candidate existing = candidateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("ứng viên không tồn tại"));

        Optional.ofNullable(dto.getFullName()).ifPresent(existing::setFullName);
        Optional.ofNullable(dto.getEmail()).ifPresent(existing::setEmail);
        Optional.ofNullable(dto.getPhone()).ifPresent(existing::setPhone);
        Optional.ofNullable(dto.getDateOfBirth()).ifPresent(existing::setDateOfBirth);
        Optional.ofNullable(dto.getGender()).ifPresent(existing::setGender);
        Optional.ofNullable(dto.getNationality()).ifPresent(existing::setNationality);
        Optional.ofNullable(dto.getIdNumber()).ifPresent(existing::setIdNumber);
        Optional.ofNullable(dto.getAddress()).ifPresent(existing::setAddress);
        Optional.ofNullable(dto.getAvatarUrl()).ifPresent(existing::setAvatarUrl);
        Optional.ofNullable(dto.getHighestEducation()).ifPresent(existing::setHighestEducation);
        Optional.ofNullable(dto.getUniversity()).ifPresent(existing::setUniversity);
        Optional.ofNullable(dto.getGraduationYear()).ifPresent(existing::setGraduationYear);
        Optional.ofNullable(dto.getGpa()).ifPresent(existing::setGpa);
        Optional.ofNullable(dto.getNotes()).ifPresent(existing::setNotes);
        Optional.ofNullable(dto.getStage()).ifPresent(existing::setStage);

        Candidate saved = candidateRepository.save(existing);
        return CandidateDetailResponseDTO.fromEntity(saved);
    }

    @Transactional
    public void delete(Long id) throws IdInvalidException {
        Candidate existing = candidateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("ứng viên không tồn tại"));
        candidateRepository.delete(existing);
    }

    @Transactional
    public CandidateDetailResponseDTO changeStage(Long id, CandidateStage stage) throws IdInvalidException {
        Candidate existing = candidateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("ứng viên không tồn tại"));
        existing.setStage(stage);
        Candidate saved = candidateRepository.save(existing);
        return CandidateDetailResponseDTO.fromEntity(saved);
    }

    public List<CandidateDetailResponseDTO> getByIds(List<Long> ids) {
        return candidateRepository.findAllById(ids).stream().map(CandidateDetailResponseDTO::fromEntity).toList();
    }
}
