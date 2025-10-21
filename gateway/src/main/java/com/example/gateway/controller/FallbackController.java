package com.example.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "User Service is temporarily unavailable");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "user-service");
        response.put("status", "SERVICE_UNAVAILABLE");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/job-service")
    public ResponseEntity<Map<String, Object>> jobServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Job Service is temporarily unavailable");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "job-service");
        response.put("status", "SERVICE_UNAVAILABLE");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/candidate-service")
    public ResponseEntity<Map<String, Object>> candidateServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Candidate Service is temporarily unavailable");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "candidate-service");
        response.put("status", "SERVICE_UNAVAILABLE");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
