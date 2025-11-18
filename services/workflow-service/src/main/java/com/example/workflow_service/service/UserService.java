package com.example.workflow_service.service;

import com.example.workflow_service.dto.Response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final RestTemplate restTemplate;

    @Value("${user-service.url:http://localhost:8082}")
    private String userServiceBaseUrl;

    /**
     * Lấy danh sách tên position theo danh sách IDs
     * Gọi user-service một lần để lấy tất cả position names
     * 
     * @param positionIds Danh sách position IDs
     * @param token       JWT token để xác thực
     * @return Map với key là positionId và value là positionName
     */
    public Map<Long, String> getPositionNamesByIds(List<Long> positionIds, String token) {
        if (positionIds == null || positionIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            // Tạo query string với các IDs
            String idsParam = positionIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            String url = UriComponentsBuilder.fromUriString(userServiceBaseUrl)
                    .path("/api/v1/user-service/positions")
                    .queryParam("ids", idsParam)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            if (token != null && !token.isEmpty()) {
                headers.setBearerAuth(token);
            }

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // User-service trả về Response<List<PositionDTO>> với field data
            ResponseEntity<Response<List<PositionDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Response<List<PositionDTO>>>() {
                    });

            Response<List<PositionDTO>> responseBody = response.getBody();
            if (responseBody == null || responseBody.getData() == null) {
                return new HashMap<>();
            }

            List<PositionDTO> positions = responseBody.getData();

            // Tạo Map từ positionId -> positionName
            return positions.stream()
                    .filter(p -> p.getId() != null && p.getName() != null)
                    .collect(Collectors.toMap(
                            PositionDTO::getId,
                            PositionDTO::getName,
                            (existing, replacement) -> existing // Nếu trùng key, giữ giá trị cũ
                    ));
        } catch (Exception ex) {
            // Log error và trả về map rỗng
            System.err.println("Error fetching position names: " + ex.getMessage());
            return new HashMap<>();
        }
    }

    @Getter
    @Setter
    private static class PositionDTO {
        private Long id;
        private String name;
        private String level;
        private Boolean active;
    }
}
