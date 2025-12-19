package com.example.statistics_service.service.client;

import com.example.statistics_service.dto.PaginationDTO;
import com.example.statistics_service.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidateServiceClient {
    private final RestTemplate restTemplate;

    @Value("${candidate-service.url:http://localhost:8084}")
    private String candidateServiceBaseUrl;

    public PaginationDTO getCandidates(String token, int page, int limit) {
        try {
            String url = UriComponentsBuilder.fromUriString(candidateServiceBaseUrl)
                    .path("/api/v1/candidate-service/candidates")
                    .queryParam("page", page)
                    .queryParam("limit", limit)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            if (token != null && !token.isEmpty()) {
                headers.setBearerAuth(token);
            }

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<Response<PaginationDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<Response<PaginationDTO>>() {
                    });

            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
            return null;
        } catch (Exception ex) {
            System.err.println("Error fetching candidates: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public PaginationDTO getApplications(String token, String status, Long departmentId, Long jobPositionId, int page,
            int limit) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(candidateServiceBaseUrl)
                    .path("/api/v1/candidate-service/applications")
                    .queryParam("page", page)
                    .queryParam("limit", limit);

            if (status != null) {
                builder.queryParam("status", status);
            }
            if (departmentId != null) {
                builder.queryParam("departmentId", departmentId);
            }
            if (jobPositionId != null) {
                builder.queryParam("jobPositionId", jobPositionId);
            }

            String url = builder.build().toUriString();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            if (token != null && !token.isEmpty()) {
                headers.setBearerAuth(token);
            }

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<Response<PaginationDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<Response<PaginationDTO>>() {
                    });

            if (response.getBody() != null && response.getBody().getData() != null) {
                PaginationDTO paginationDTO = response.getBody().getData();
                return paginationDTO;
            }
            return null;
        } catch (Exception ex) {
            System.err.println("Error fetching applications: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
}
