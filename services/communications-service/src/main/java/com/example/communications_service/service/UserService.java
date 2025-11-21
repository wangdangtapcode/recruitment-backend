package com.example.communications_service.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

    public ResponseEntity<JsonNode> getEmployeeName(Long employeeId, String token) {
        try {
            String url = userServiceUrl + "/api/v1/user-service/employees/" + employeeId;

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

    public ResponseEntity<JsonNode> getEmployeeNames(List<Long> employeeIds, String token) {
        try {
            String url = userServiceUrl + "/api/v1/user-service/employees?ids="
                    + employeeIds.stream().map(String::valueOf).collect(Collectors.joining(","));

            HttpHeaders headers = new HttpHeaders();
            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", "Bearer " + token);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data"); // expected array of employees
            ObjectNode idToName = objectMapper.createObjectNode();
            if (data != null && data.isArray()) {
                for (JsonNode employee : data) {
                    if (employee.has("id") && employee.has("name")) {
                        idToName.put(String.valueOf(employee.get("id").asLong()), employee.get("name").asText());
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

    /**
     * Lấy userId (employeeId) từ email
     * Gọi API user-service để tìm employee theo email
     */
    public Long getUserIdByEmail(String email) {
        try {
            String url = userServiceUrl + "/api/v1/user-service/users/email/" + email;

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");

            if (data != null && data.has("employeeId")) {
                return data.get("employeeId").asLong();
            }

            // Nếu không có employeeId, thử tìm trực tiếp employee
            if (data != null && data.has("id")) {
                return data.get("id").asLong();
            }

            return null;
        } catch (Exception e) {
            // Nếu không tìm thấy hoặc lỗi, trả về null (có thể là email bên ngoài)
            return null;
        }
    }

    public List<Long> getEmployeeIdsByFilters(Long departmentId, Long positionId, String authToken) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(userServiceUrl + "/api/v1/user-service/employees")
                    .queryParam("page", 1)
                    .queryParam("limit", 1000);

            if (departmentId != null) {
                builder.queryParam("departmentId", departmentId);
            }
            if (positionId != null) {
                builder.queryParam("positionId", positionId);
            }

            String url = builder.toUriString();
            System.out.println("url: " + url);
            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);
            System.out.println("response: " + response.getBody());
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.path("data").path("result");
            System.out.println("data: " + data);
            List<Long> result = new ArrayList<>();
            if (data != null && data.isArray()) {
                data.forEach(node -> {
                    if (node.has("id")) {
                        result.add(node.get("id").asLong());
                    }
                });
            }
            return result;
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Long> getAllEmployeeIds(String authToken) {
        return getEmployeeIdsByFilters(null, null, authToken);
    }

}
