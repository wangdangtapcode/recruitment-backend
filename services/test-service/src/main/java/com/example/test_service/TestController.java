package com.example.test_service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test-service")
public class TestController {

    @GetMapping("/")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test");
    }
    
}
