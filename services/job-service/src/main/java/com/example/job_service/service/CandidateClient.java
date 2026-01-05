package com.example.job_service.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.job_service.dto.Response;

@Service
public class CandidateClient {

    private final RestTemplate restTemplate;

    @Value("${candidate-service.url:http://localhost:8081}")
    private String candidateServiceUrl;

    public CandidateClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Đếm số lượng ứng viên theo jobPositionId
     * Sử dụng endpoint /count để tránh vòng lặp (không gọi lại job-service)
     * 
     * @param jobPositionId ID của vị trí tuyển dụng
     * @param token         JWT token (có thể null cho public API)
     * @return Số lượng ứng viên
     */
    public Integer countCandidatesByJobPositionId(Long jobPositionId, String token) {
        try {
            String url = candidateServiceUrl + "/api/v1/candidate-service/candidates/count?jobPositionId="
                    + jobPositionId;

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            if (token != null && !token.isEmpty()) {
                headers.setBearerAuth(token);
            }

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Response<Long>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<Response<Long>>() {
                    });

            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData().intValue();
            }
            return 0;
        } catch (Exception e) {
            // Nếu lỗi, trả về 0
            return 0;
        }
    }

    /**
     * Lấy số lượng ứng viên cho nhiều jobPositionIds cùng lúc
     * 
     * @param jobPositionIds Danh sách ID vị trí tuyển dụng
     * @param token          JWT token
     * @return Map với key là jobPositionId và value là số lượng ứng viên
     */
    public Map<Long, Integer> countCandidatesByJobPositionIds(List<Long> jobPositionIds, String token) {
        Map<Long, Integer> counts = new HashMap<>();

        if (jobPositionIds == null || jobPositionIds.isEmpty()) {
            return counts;
        }

        // Gọi song song cho từng jobPositionId
        for (Long jobPositionId : jobPositionIds) {
            try {
                Integer count = countCandidatesByJobPositionId(jobPositionId, token);
                counts.put(jobPositionId, count);
            } catch (Exception e) {
                // Nếu lỗi, set về 0
                counts.put(jobPositionId, 0);
            }
        }

        return counts;
    }
}
