package com.example.job_service.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class UserService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${user-service.url:http://localhost:8082}")
    private String userServiceUrl;

    public UserService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public ResponseEntity<JsonNode> getUserById(Long userId, String token) {
        try {
            String url = userServiceUrl + "/api/v1/user-service/users/" + userId;

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
    public ResponseEntity<JsonNode> getPublicDepartmentById(Long departmentId) {
        try {
            String url = userServiceUrl + "/api/v1/user-service/departments/public/" + departmentId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.has("data") ? root.get("data") : root;
            return ResponseEntity.ok(dataNode);
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
    public ResponseEntity<JsonNode> getDepartmentById(Long departmentId, String token) {
        try {
            String url = userServiceUrl + "/api/v1/user-service/departments/" + departmentId;
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

    public Map<Long, JsonNode> getUsersByIds(List<Long> userIds, String token) {
        return userIds.stream().collect(Collectors.toMap(
                id -> id,
                id -> {
                    try {
                        // Hàm getUserById sẽ ném lỗi nếu user-service trả lỗi
                        ResponseEntity<JsonNode> response = getUserById(id, token);
                        return response.getBody();
                    } catch (HttpClientErrorException | HttpServerErrorException ex) {
                        // Trả nguyên JSON body từ user-service ra ngoài
                        throw new ResponseStatusException(
                                ex.getStatusCode(),
                                ex.getResponseBodyAsString());
                    }
                }));
    }

    public ResponseEntity<JsonNode> getAllDepartments(String token) {
        try {
            String url = userServiceUrl + "/api/v1/user-service/departments";

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

    public Map<Long, String> getDepartmentsByIds(List<Long> departmentIds, String token) {
        Map<Long, String> departmentNames = new HashMap<>();

        if (departmentIds.isEmpty()) {
            return departmentNames;
        }

        try {
            // Create comma-separated string of department IDs
            String idsParam = departmentIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            String url = userServiceUrl + "/api/v1/user-service/departments?ids=" + idsParam;

            HttpHeaders headers = new HttpHeaders();
            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", "Bearer " + token);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode dataNode = root.has("data") ? root.get("data") : root;

                if (dataNode.isArray()) {
                    for (JsonNode department : dataNode) {
                        Long deptId = department.has("id") ? department.get("id").asLong() : null;
                        String name = department.has("name") ? department.get("name").asText() : "Unknown Department";
                        if (deptId != null) {
                            departmentNames.put(deptId, name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // If failed to get departments, set default names
            for (Long deptId : departmentIds) {
                departmentNames.put(deptId, "Unknown Department");
            }
        }

        // Set default names for any missing departments
        for (Long deptId : departmentIds) {
            departmentNames.putIfAbsent(deptId, "Unknown Department");
        }

        return departmentNames;
    }

}
