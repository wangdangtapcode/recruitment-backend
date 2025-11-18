package com.example.candidate_service.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.candidate_service.dto.Response;
import com.example.candidate_service.utils.SecurityUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final CacheManager cacheManager;
    private final RestTemplate restTemplate;

    @Value("${user-service.url:http://localhost:8082}")
    private String userServiceBaseUrl;

    public PermissionInterceptor(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (path == null) {
            path = request.getRequestURI();
        }
        String httpMethod = request.getMethod();
        String permissionName = extractPermissionName(path, httpMethod);

        @SuppressWarnings("unchecked")
        Map<String, Boolean> memo = (Map<String, Boolean>) request.getAttribute("_permMemo");
        if (memo == null) {
            memo = new HashMap<>();
            request.setAttribute("_permMemo", memo);
        }
        Boolean cached = memo.get(permissionName);
        boolean allowed;
        if (cached != null) {
            allowed = cached.booleanValue();
        } else {
            allowed = checkPermission(permissionName);
            System.out.println("allowed: " + allowed);
            memo.put(permissionName, allowed);
        }
        if (!allowed) {
            throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
        }

        return true;
    }

    private String extractPermissionName(String path, String httpMethod) {
        try {
            String[] segments = path.split("/");
            String service = segments[3];
            String resource = segments[4];
            String action = mapHttpMethodToAction(httpMethod);
            return service + ":" + resource + ":" + action;
        } catch (Exception e) {
            return "unknown:unknown:unknown";
        }
    }

    private String mapHttpMethodToAction(String method) {
        return switch (method) {
            case "GET" -> "read";
            case "POST" -> "create";
            case "PUT", "PATCH" -> "update";
            case "DELETE" -> "delete";
            default -> "unknown";
        };
    }

    private boolean checkPermission(String permissionName) {
        String token = SecurityUtil.getCurrentUserJWT().orElse(null);
        if (token == null) {
            return false;
        }

        Cache cache = cacheManager.getCache("permCheck");
        String cacheKey = token + ":" + permissionName;
        if (cache != null) {
            Boolean cached = cache.get(cacheKey, Boolean.class);
            if (cached != null) {
                return cached;
            }
        }

        boolean allowed = callUserService(permissionName, token);
        if (cache != null) {
            cache.put(cacheKey, allowed);
        }
        return allowed;
    }

    private boolean callUserService(String permissionName, String token) {
        try {
            String url = UriComponentsBuilder.fromUriString(userServiceBaseUrl)
                    .path("/api/v1/user-service/auth/check")
                    .queryParam("name", permissionName)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<Response<Boolean>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new org.springframework.core.ParameterizedTypeReference<Response<Boolean>>() {
                    });
            Response<Boolean> body = response.getBody();
            return body != null && Boolean.TRUE.equals(body.getData());
        } catch (Exception ex) {
            return false;
        }
    }
}