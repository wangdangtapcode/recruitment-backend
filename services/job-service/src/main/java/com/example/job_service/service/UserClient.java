package com.example.job_service.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class UserClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${user-service.url:http://localhost:8082}")
    private String userServiceUrl;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public ResponseEntity<JsonNode> getEmployeeById(Long employeeId, String token) {
        try {
            String url = userServiceUrl + "/api/v1/user-service/employees/" + employeeId;

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
            return ResponseEntity.notFound().build();
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
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Response<JsonNode>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<Response<JsonNode>>() {
                    });
            if (response.getBody() != null && response.getBody().getData() != null) {
                return ResponseEntity.ok(response.getBody().getData());
            }
            return ResponseEntity.notFound().build();
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
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("statusCode", 500);
            errorNode.put("error", "Internal Server Error");
            errorNode.put("message", "Không thể kết nối tới User Service: " + e.getMessage());
            errorNode.putNull("data");
            return ResponseEntity.internalServerError().body(errorNode);
        }
    }

    public Map<Long, JsonNode> getEmployeesByIds(List<Long> employeeIds, String token) {
        return employeeIds.stream().collect(Collectors.toMap(
                id -> id,
                id -> {
                    // Hàm getEmployeeById sẽ ném lỗi nếu user-service trả lỗi
                    ResponseEntity<JsonNode> response = getEmployeeById(id, token);
                    return response.getBody();
                }));
    }

    public ResponseEntity<JsonNode> getAllDepartments(String token) {
        try {
            String url = userServiceUrl + "/api/v1/user-service/departments";

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
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            if (token != null && !token.isEmpty()) {
                headers.setBearerAuth(token);
            }

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Response<List<Map<String, Object>>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<Response<List<Map<String, Object>>>>() {
                    });

            if (response.getBody() != null && response.getBody().getData() != null) {
                List<Map<String, Object>> departments = response.getBody().getData();
                for (Map<String, Object> department : departments) {
                    Object deptId = department.get("id");
                    Object name = department.get("name");
                    if (deptId instanceof Number) {
                        Long id = ((Number) deptId).longValue();
                        String deptName = name != null ? name.toString() : "Unknown Department";
                        departmentNames.put(id, deptName);
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
