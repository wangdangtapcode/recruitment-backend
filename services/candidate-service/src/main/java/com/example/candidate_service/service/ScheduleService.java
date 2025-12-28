package com.example.candidate_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.candidate_service.dto.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class ScheduleService {

    @Value("${schedule-service.url:http://localhost:8085}")
    private String scheduleServiceUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ScheduleService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // Lấy các lịch (detailed) theo participant (CANDIDATE)
    public ResponseEntity<JsonNode> getUpcomingSchedulesForCandidate(Long candidateId, String token) {
        try {
            String url = scheduleServiceUrl
                    + "/api/v1/schedule-service/schedules?participantId=" + candidateId
                    + "&participantType=CANDIDATE&status=SCHEDULED";
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            if (token != null && !token.isEmpty()) {
                headers.setBearerAuth(token);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Response<JsonNode>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<Response<JsonNode>>() {
                    });
            if (response.getBody() != null && response.getBody().getData() != null) {
                return ResponseEntity.ok(response.getBody().getData());
            }
            return ResponseEntity.ok(objectMapper.createArrayNode());
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
