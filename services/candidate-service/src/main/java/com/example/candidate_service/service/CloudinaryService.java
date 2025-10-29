package com.example.candidate_service.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> upload(MultipartFile file) throws IOException {
        return (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
    }
    public String uploadFile(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "resource_type", "auto",
                    "type", "upload"
                )
            );
            return (String) uploadResult.get("secure_url"); // Lấy URL an toàn (https)
        } catch (IOException e) {
            throw new RuntimeException("Không thể upload file", e);
        }
    }
}