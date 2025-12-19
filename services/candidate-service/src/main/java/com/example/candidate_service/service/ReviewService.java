package com.example.candidate_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.candidate_service.dto.Meta;
import com.example.candidate_service.dto.PaginationDTO;
import com.example.candidate_service.dto.review.CreateReviewDTO;
import com.example.candidate_service.dto.review.ReviewResponseDTO;
import com.example.candidate_service.dto.review.UpdateReviewDTO;
import com.example.candidate_service.exception.IdInvalidException;
import com.example.candidate_service.model.Application;
import com.example.candidate_service.model.Review;
import com.example.candidate_service.repository.ApplicationRepository;
import com.example.candidate_service.repository.ReviewRepository;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ApplicationRepository applicationRepository;
    private final UserService userService;

    public ReviewService(ReviewRepository reviewRepository, ApplicationRepository applicationRepository,
            UserService userService) {
        this.reviewRepository = reviewRepository;
        this.applicationRepository = applicationRepository;
        this.userService = userService;
    }

    public List<ReviewResponseDTO> getByApplicationId(Long applicationId, String token) throws IdInvalidException {
        ensureApplicationExists(applicationId);
        List<Review> reviews = reviewRepository.findByApplication_Id(applicationId);
        return convertToResponseList(reviews, token);
    }

    public PaginationDTO getAllWithFilters(Long applicationId, Long reviewerId,
            LocalDateTime startDate, LocalDateTime endDate,
            int page, int limit, String sortBy, String sortOrder,
            String token) {
        // Validate pagination parameters
        if (page < 1)
            page = 1;
        if (limit < 1 || limit > 100)
            limit = 10;

        // Create sort object
        Sort.Direction direction = sortOrder != null && sortOrder.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String sortField = sortBy != null ? sortBy : "createdAt";
        Sort sort = Sort.by(direction, sortField);

        // Create pageable object
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        // Query with filters
        Page<Review> reviewPage = reviewRepository.findByFilters(
                applicationId, reviewerId, startDate, endDate, pageable);

        // Convert to DTOs
        List<ReviewResponseDTO> reviewDTOs = convertToResponseList(reviewPage.getContent(), token);

        // Create pagination metadata
        Meta meta = new Meta();
        meta.setPage(page);
        meta.setPageSize(limit);
        meta.setPages(reviewPage.getTotalPages());
        meta.setTotal(reviewPage.getTotalElements());

        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setMeta(meta);
        paginationDTO.setResult(reviewDTOs);

        return paginationDTO;
    }

    public ReviewResponseDTO getById(Long id, String token) throws IdInvalidException {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Đánh giá không tồn tại"));

        JsonNode idToName = null;
        if (review.getReviewerId() != null && token != null && !token.isEmpty()) {
            var response = userService.getEmployeeNames(List.of(review.getReviewerId()), token);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                idToName = response.getBody();
            }
        }

        return convertToResponse(review, idToName);
    }

    @Transactional
    public ReviewResponseDTO create(CreateReviewDTO dto, Long reviewerId) throws IdInvalidException {
        Application application = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new IdInvalidException("Đơn ứng tuyển không tồn tại"));

        Review review = new Review();
        review.setApplication(application);
        review.setReviewerId(reviewerId);
        review.setComment(dto.getComment());

        Review saved = reviewRepository.save(review);
        return convertToResponse(saved, null);
    }

    @Transactional
    public ReviewResponseDTO update(Long id, UpdateReviewDTO dto, Long reviewerId) throws IdInvalidException {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Đánh giá không tồn tại"));

        // Chỉ cho phép người tạo review mới được update
        if (!review.getReviewerId().equals(reviewerId)) {
            throw new IdInvalidException("Bạn không có quyền cập nhật đánh giá này");
        }

        if (dto.getComment() != null) {
            review.setComment(dto.getComment());
        }

        Review saved = reviewRepository.save(review);
        return convertToResponse(saved, null);
    }

    @Transactional
    public void delete(Long id, Long reviewerId) throws IdInvalidException {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Đánh giá không tồn tại"));

        // Chỉ cho phép người tạo review mới được xóa
        if (!review.getReviewerId().equals(reviewerId)) {
            throw new IdInvalidException("Bạn không có quyền xóa đánh giá này");
        }

        reviewRepository.deleteById(id);
    }

    private void ensureApplicationExists(Long applicationId) throws IdInvalidException {
        if (!applicationRepository.existsById(applicationId)) {
            throw new IdInvalidException("Đơn ứng tuyển không tồn tại");
        }
    }

    private List<ReviewResponseDTO> convertToResponseList(List<Review> reviews, String token) {
        // Batch fetch reviewer names
        Set<Long> reviewerIds = reviews.stream()
                .map(Review::getReviewerId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        JsonNode idToName = null;
        if (!reviewerIds.isEmpty() && token != null && !token.isEmpty()) {
            var response = userService.getEmployeeNames(reviewerIds.stream().toList(), token);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                idToName = response.getBody();
            }
        }

        final JsonNode finalIdToName = idToName;
        return reviews.stream()
                .map(r -> convertToResponse(r, finalIdToName))
                .collect(Collectors.toList());
    }

    private ReviewResponseDTO convertToResponse(Review review, JsonNode idToName) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setApplicationId(review.getApplication().getId());
        dto.setReviewerId(review.getReviewerId());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        // Set reviewer name
        if (idToName != null && review.getReviewerId() != null) {
            JsonNode nameNode = idToName.get(String.valueOf(review.getReviewerId()));
            if (nameNode != null) {
                dto.setReviewerName(nameNode.asText());
            }
        }

        return dto;
    }
}
