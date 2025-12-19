package com.example.candidate_service.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.candidate_service.dto.PaginationDTO;
import com.example.candidate_service.dto.candidate.CandidateDetailResponseDTO;
import com.example.candidate_service.dto.candidate.CreateCandidateDTO;
import com.example.candidate_service.dto.candidate.UpdateCandidateDTO;
import com.example.candidate_service.exception.IdInvalidException;
import com.example.candidate_service.service.CandidateService;
import com.example.candidate_service.utils.annotation.ApiMessage;
import com.example.candidate_service.utils.enums.CandidateStage;

@RestController
@RequestMapping("/api/v1/candidate-service/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping
    @ApiMessage("Lấy danh sách ứng viên với bộ lọc, phân trang và sắp xếp")
    public ResponseEntity<PaginationDTO> getAllCandidates(
            @RequestParam(name = "stage", required = false) CandidateStage stage,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "departmentId", required = false) Long departmentId,
            @RequestParam(name = "page", defaultValue = "1", required = false) int page,
            @RequestParam(name = "limit", defaultValue = "10", required = false) int limit,
            @RequestParam(name = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = "desc", required = false) String sortOrder) {

        if (page < 1)
            page = 1;
        if (limit < 1 || limit > 100)
            limit = 10;

        Sort.Direction direction = sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        PaginationDTO paginationDTO = candidateService.getAllWithFilters(stage, keyword, departmentId, pageable);

        return ResponseEntity.ok(paginationDTO);
    }

    @GetMapping("/{id}")
    @ApiMessage("Lấy thông tin ứng viên")
    public ResponseEntity<CandidateDetailResponseDTO> getById(@PathVariable Long id) throws IdInvalidException {
        CandidateDetailResponseDTO candidate = candidateService.getById(id);
        return ResponseEntity.ok(candidate);
    }

    @PostMapping
    @ApiMessage("Tạo mới ứng viên")
    public ResponseEntity<CandidateDetailResponseDTO> create(@Validated @RequestBody CreateCandidateDTO dto) {
        CandidateDetailResponseDTO saved = candidateService.create(dto);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật thông tin ứng viên")
    public ResponseEntity<CandidateDetailResponseDTO> update(@PathVariable Long id,
            @Validated @RequestBody UpdateCandidateDTO dto)
            throws IdInvalidException {
        CandidateDetailResponseDTO saved = candidateService.update(id, dto);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xóa ứng viên")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IdInvalidException {
        candidateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/stage")
    @ApiMessage("Cập nhật giai đoạn (stage) của ứng viên")
    public ResponseEntity<CandidateDetailResponseDTO> changeStage(@PathVariable Long id,
            @RequestParam CandidateStage stage) throws IdInvalidException {
        CandidateDetailResponseDTO saved = candidateService.changeStage(id, stage);
        return ResponseEntity.ok(saved);
    }

    @GetMapping(params = "ids")
    public ResponseEntity<List<CandidateDetailResponseDTO>> findByIds(@RequestParam("ids") String ids) {
        List<Long> candidateIds = List.of(ids.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
        return ResponseEntity.ok(this.candidateService.getByIds(candidateIds));
    }
}
