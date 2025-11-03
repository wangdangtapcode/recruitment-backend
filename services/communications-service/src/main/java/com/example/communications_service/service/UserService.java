package com.example.communications_service.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class UserService {

    @Value("${user-service.url:http://localhost:8082}")
    private String userServiceUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public UserService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<JsonNode> getUserName(Long userId, String token) {
        try {
            String url = userServiceUrl + "/api/v1/user-service/users/" + userId;

            HttpHeaders headers = new HttpHeaders();
            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", "Bearer " + token);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");
            return ResponseEntity.ok(data.get("name"));

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            try {
                JsonNode errorBody = objectMapper.readTree(ex.getResponseBodyAsString());
                return ResponseEntity.status(ex.getStatusCode()).body(errorBody);
            } catch (Exception parseEx) {
                ObjectNode fallback = objectMapper.createObjectNode();
                fallback.put("statusCode", ex.getStatusCode().value());
                fallback.put("error", ex.getStatusText());
                fallback.put("message", "Không thể parse phản hồi lỗi từ User Service");
                fallback.putNull("data");
                return ResponseEntity.status(ex.getStatusCode()).body(fallback);
            }
        } catch (Exception e) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("statusCode", 500);
            errorNode.put("error", "Internal Server Error");
            errorNode.put("message", "Không thể kết nối tới User Service: " + e.getMessage());
            errorNode.putNull("data");
            return ResponseEntity.internalServerError().body(errorNode);
        }
    }

    public ResponseEntity<JsonNode> getUserNames(List<Long> userIds, String token) {
        try {
            String url = userServiceUrl + "/api/v1/user-service/users?ids="
                    + userIds.stream().map(String::valueOf).collect(Collectors.joining(","));

            HttpHeaders headers = new HttpHeaders();
            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", "Bearer " + token);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data"); // expected array of users
            ObjectNode idToName = objectMapper.createObjectNode();
            if (data != null && data.isArray()) {
                for (JsonNode user : data) {
                    if (user.has("id") && user.has("name")) {
                        idToName.put(String.valueOf(user.get("id").asLong()), user.get("name").asText());
                    }
                }
            }
            return ResponseEntity.ok(idToName);

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            try {
                JsonNode errorBody = objectMapper.readTree(ex.getResponseBodyAsString());
                return ResponseEntity.status(ex.getStatusCode()).body(errorBody);
            } catch (Exception parseEx) {
                ObjectNode fallback = objectMapper.createObjectNode();
                fallback.put("statusCode", ex.getStatusCode().value());
                fallback.put("error", ex.getStatusText());
                fallback.put("message", "Không thể parse phản hồi lỗi từ User Service");
                fallback.putNull("data");
                return ResponseEntity.status(ex.getStatusCode()).body(fallback);
            }
        } catch (Exception e) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("statusCode", 500);
            errorNode.put("error", "Internal Server Error");
            errorNode.put("message", "Không thể kết nối tới User Service: " + e.getMessage());
            errorNode.putNull("data");
            return ResponseEntity.internalServerError().body(errorNode);
        }
    }

}
