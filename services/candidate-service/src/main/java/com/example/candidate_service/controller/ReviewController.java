package com.example.candidate_service.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.candidate_service.dto.PaginationDTO;
import com.example.candidate_service.dto.review.CreateReviewDTO;
import com.example.candidate_service.dto.review.ReviewResponseDTO;
import com.example.candidate_service.dto.review.UpdateReviewDTO;
import com.example.candidate_service.exception.IdInvalidException;
import com.example.candidate_service.service.ReviewService;
import com.example.candidate_service.utils.SecurityUtil;
import com.example.candidate_service.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/candidate-service/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    @ApiMessage("Lấy danh sách đánh giá với filter và pagination")
    public ResponseEntity<PaginationDTO> getAll(
            @RequestParam(name = "applicationId", required = false) Long applicationId,
            @RequestParam(name = "reviewerId", required = false) Long reviewerId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(name = "page", defaultValue = "1", required = false) int page,
            @RequestParam(name = "limit", defaultValue = "10", required = false) int limit,
            @RequestParam(name = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = "desc", required = false) String sortOrder) {
        String token = SecurityUtil.getCurrentUserJWT().orElse("");
        PaginationDTO result = reviewService.getAllWithFilters(
                applicationId, reviewerId, startDate, endDate, page, limit, sortBy, sortOrder, token);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/application/{applicationId}")
    @ApiMessage("Lấy danh sách đánh giá theo application")
    public ResponseEntity<List<ReviewResponseDTO>> getByApplication(@PathVariable Long applicationId)
            throws IdInvalidException {
        String token = SecurityUtil.getCurrentUserJWT().orElse("");
        return ResponseEntity.ok(reviewService.getByApplicationId(applicationId, token));
    }

    @GetMapping("/{id}")
    @ApiMessage("Lấy chi tiết đánh giá")
    public ResponseEntity<ReviewResponseDTO> getById(@PathVariable Long id) throws IdInvalidException {
        String token = SecurityUtil.getCurrentUserJWT().orElse("");
        return ResponseEntity.ok(reviewService.getById(id, token));
    }

    @PostMapping
    @ApiMessage("Tạo đánh giá")
    public ResponseEntity<ReviewResponseDTO> create(@Validated @RequestBody CreateReviewDTO dto)
            throws IdInvalidException {
        Long reviewerId = SecurityUtil.extractEmployeeId();
        return ResponseEntity.ok(reviewService.create(dto, reviewerId));
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật đánh giá")
    public ResponseEntity<ReviewResponseDTO> update(@PathVariable Long id,
            @Validated @RequestBody UpdateReviewDTO dto) throws IdInvalidException {
        Long reviewerId = SecurityUtil.extractEmployeeId();
        return ResponseEntity.ok(reviewService.update(id, dto, reviewerId));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xóa đánh giá")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IdInvalidException {
        Long reviewerId = SecurityUtil.extractEmployeeId();
        reviewService.delete(id, reviewerId);
        return ResponseEntity.noContent().build();
    }
}
