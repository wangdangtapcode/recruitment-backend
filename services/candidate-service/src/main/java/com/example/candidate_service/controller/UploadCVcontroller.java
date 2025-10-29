package com.example.candidate_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import com.example.candidate_service.dto.application.ApplicationResponseDTO;
import com.example.candidate_service.dto.application.UploadCVDTO;
import com.example.candidate_service.dto.application.UpdateApplicationDTO;
import com.example.candidate_service.exception.IdInvalidException;
import com.example.candidate_service.service.ApplicationService;
import com.example.candidate_service.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/candidate-service/public")
public class UploadCVcontroller {

    private final ApplicationService applicationService;

    public UploadCVcontroller(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/upload-cv")
    @ApiMessage("Upload CV với file (Public API)")
    public ResponseEntity<ApplicationResponseDTO> uploadCVWithFile(@Validated @ModelAttribute UploadCVDTO dto)
            throws IdInvalidException, IOException {
        return ResponseEntity.ok(applicationService.uploadCVWithFile(dto));
    }

    @PutMapping("/applications/{id}")
    @ApiMessage("Cập nhật đơn ứng tuyển (Public API)")
    public ResponseEntity<ApplicationResponseDTO> updatePublic(
            @PathVariable Long id,
            @Validated @ModelAttribute UpdateApplicationDTO dto) throws IdInvalidException, IOException {
        return ResponseEntity.ok(applicationService.updateApplication(id, dto));
    }

    @DeleteMapping("/applications/{id}")
    @ApiMessage("Xóa đơn ứng tuyển (Public API)")
    public ResponseEntity<Void> deletePublic(@PathVariable Long id) throws IdInvalidException {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}
