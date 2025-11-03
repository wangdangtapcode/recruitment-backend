package com.example.candidate_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class JobService {
    @Value("${job-service.url:http://localhost:8083}")
    private String jobServiceUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public JobService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<JsonNode> getJobPositionById(Long id, String token) {
        try {
            String url = jobServiceUrl + "/api/v1/job-service/job-positions/" + id;
            HttpHeaders headers = new HttpHeaders();
            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", "Bearer " + token);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.has("data") ? root.get("data") : root;

            return ResponseEntity.status(response.getStatusCode()).body(dataNode);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            try {
                JsonNode errorBody = objectMapper.readTree(ex.getResponseBodyAsString());
                return ResponseEntity.status(ex.getStatusCode()).body(errorBody);
            } catch (Exception parseEx) {
                ObjectNode fallback = objectMapper.createObjectNode();
                fallback.put("statusCode", ex.getStatusCode().value());
                fallback.put("error", ex.getStatusText());
                fallback.put("message", "Không thể parse phản hồi lỗi từ Job Service");
                fallback.putNull("data");
                return ResponseEntity.status(ex.getStatusCode()).body(fallback);
            }
        } catch (Exception e) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("statusCode", 500);
            errorNode.put("error", "Internal Server Error");
            errorNode.put("message", "Không thể kết nối tới Job Service: " + e.getMessage());
            errorNode.putNull("data");
            return ResponseEntity.internalServerError().body(errorNode);
        }
    }
}
