package com.example.candidate_service.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.candidate_service.service.CloudinaryService;

@RestController
@RequestMapping("/api/v1/candidate-service/upload")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    public UploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping
    public ResponseEntity<Map> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        Map uploadResult = cloudinaryService.upload(file);
        return ResponseEntity.ok(uploadResult);
    }
}
