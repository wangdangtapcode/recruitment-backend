package com.example.job_service.exception;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

public class UserServiceException extends RuntimeException {
    private final ResponseEntity<JsonNode> responseEntity;

    public UserServiceException(ResponseEntity<JsonNode> responseEntity) {
        this.responseEntity = responseEntity;
    }

    public ResponseEntity<JsonNode> getResponseEntity() {
        return responseEntity;
    }
}

