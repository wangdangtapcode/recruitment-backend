package com.example.candidate_service.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.candidate_service.dto.comment.CommentResponseDTO;
import com.example.candidate_service.dto.comment.CreateCommentDTO;
import com.example.candidate_service.dto.comment.UpdateCommentDTO;
import com.example.candidate_service.exception.IdInvalidException;
import com.example.candidate_service.model.Application;
import com.example.candidate_service.model.Comment;
import com.example.candidate_service.repository.ApplicationRepository;
import com.example.candidate_service.repository.CommentRepository;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ApplicationRepository applicationRepository;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository, ApplicationRepository applicationRepository,
            UserService userService) {
        this.commentRepository = commentRepository;
        this.applicationRepository = applicationRepository;
        this.userService = userService;
    }

    public List<CommentResponseDTO> getByApplicationId(Long applicationId, String token) throws IdInvalidException {
        ensureApplicationExists(applicationId);
        List<Comment> comments = commentRepository.findByApplication_Id(applicationId);

        // Batch fetch user names
        Set<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        JsonNode idToName = null;
        if (!userIds.isEmpty()) {
            idToName = userService.getUserNames(userIds.stream().toList(), token).getBody();
        }

        final JsonNode finalIdToName = idToName;
        return comments.stream()
                .map(c -> toResponse(c, finalIdToName))
                .collect(Collectors.toList());
    }

    public CommentResponseDTO getById(Long id) throws IdInvalidException {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Bình luận không tồn tại"));
        return toResponse(comment, null);
    }

    @Transactional
    public CommentResponseDTO create(CreateCommentDTO dto, Long userId) throws IdInvalidException {
        Application application = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new IdInvalidException("Đơn ứng tuyển không tồn tại"));
        Comment c = new Comment();
        c.setApplication(application);
        c.setUserId(userId);
        c.setContent(dto.getContent());
        Comment saved = commentRepository.save(c);
        return toResponse(saved, null);
    }

    @Transactional
    public CommentResponseDTO update(Long id, UpdateCommentDTO dto, Long userId) throws IdInvalidException {
        Comment c = commentRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Bình luận không tồn tại"));
        if (dto.getContent() != null) {
            c.setContent(dto.getContent());
        }
        Comment saved = commentRepository.save(c);
        return toResponse(saved, null);
    }

    @Transactional
    public void delete(Long id) throws IdInvalidException {
        if (!commentRepository.existsById(id)) {
            throw new IdInvalidException("Bình luận không tồn tại");
        }
        commentRepository.deleteById(id);
    }

    private void ensureApplicationExists(Long applicationId) throws IdInvalidException {
        if (!applicationRepository.existsById(applicationId)) {
            throw new IdInvalidException("Đơn ứng tuyển không tồn tại");
        }
    }

    private CommentResponseDTO toResponse(Comment c, JsonNode idToName) {
        CommentResponseDTO d = new CommentResponseDTO();
        d.setId(c.getId());
        d.setUserId(c.getUserId());
        d.setContent(c.getContent());
        d.setCreatedAt(c.getCreatedAt());
        if (idToName != null && c.getUserId() != null) {
            JsonNode nameNode = idToName.get(String.valueOf(c.getUserId()));
            if (nameNode != null) {
                d.setUserName(nameNode.asText());
            }
        }
        return d;
    }
}
