package com.example.candidate_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class CommunicationService {

    @Value("${communications-service.url:http://localhost:8085}")
    private String communicationsServiceUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public CommunicationService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // Lấy các lịch (detailed) theo participant (CANDIDATE)
    public ResponseEntity<JsonNode> getUpcomingSchedulesForCandidate(Long candidateId, String token) {
        try {
            String url = communicationsServiceUrl
                    + "/api/v1/communications-service/schedules?participantId=" + candidateId
                    + "&participantType=CANDIDATE&status=SCHEDULED";
            HttpHeaders headers = new HttpHeaders();
            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", "Bearer " + token);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            // Giả định API trả về mảng schedules
            return ResponseEntity.status(response.getStatusCode()).body(root);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            try {
                JsonNode errorBody = objectMapper.readTree(ex.getResponseBodyAsString());
                return ResponseEntity.status(ex.getStatusCode()).body(errorBody);
            } catch (Exception parseEx) {
                ObjectNode fallback = objectMapper.createObjectNode();
                fallback.put("statusCode", ex.getStatusCode().value());
                fallback.put("error", ex.getStatusText());
                fallback.put("message", "Không thể parse phản hồi lỗi từ Communications Service");
                fallback.putNull("data");
                return ResponseEntity.status(ex.getStatusCode()).body(fallback);
            }
        } catch (Exception e) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("statusCode", 500);
            errorNode.put("error", "Internal Server Error");
            errorNode.put("message", "Không thể kết nối tới Communications Service: " + e.getMessage());
            errorNode.putNull("data");
            return ResponseEntity.internalServerError().body(errorNode);
        }
    }
}
