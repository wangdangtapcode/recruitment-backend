package com.example.statistics_service.service.client;

import com.example.statistics_service.dto.Response;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunicationServiceClient {
    private final RestTemplate restTemplate;

    @Value("${communications-service.url:http://localhost:8085}")
    private String communicationsServiceBaseUrl;

    public List<JsonNode> getSchedules(String token, LocalDate startDate, LocalDate endDate, 
                                       Long participantId, String participantType) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(communicationsServiceBaseUrl)
                    .path("/api/v1/communications-service/schedules");
            
            if (startDate != null) {
                builder.queryParam("startDate", startDate);
            }
            if (endDate != null) {
                builder.queryParam("endDate", endDate);
            }
            if (participantId != null) {
                builder.queryParam("participantId", participantId);
            }
            if (participantType != null) {
                builder.queryParam("participantType", participantType);
            }
            
            String url = builder.build().toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            if (token != null && !token.isEmpty()) {
                headers.setBearerAuth(token);
            }

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<Response<List<JsonNode>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, 
                    new ParameterizedTypeReference<Response<List<JsonNode>>>() {});

            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
            return List.of();
        } catch (Exception ex) {
            System.err.println("Error fetching schedules: " + ex.getMessage());
            return List.of();
        }
    }

    public List<JsonNode> getUpcomingSchedules(String token, int limit) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate nextWeek = today.plusDays(7);
            
            return getSchedules(token, today, nextWeek, null, null);
        } catch (Exception ex) {
            System.err.println("Error fetching upcoming schedules: " + ex.getMessage());
            return List.of();
        }
    }
}

