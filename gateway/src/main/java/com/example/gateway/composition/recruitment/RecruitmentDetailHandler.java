package com.example.gateway.composition.recruitment;

import com.example.gateway.composition.core.CompositionHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RecruitmentDetailHandler implements CompositionHandler {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public RecruitmentDetailHandler(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
        this.objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                false);
    }

    @Override
    public boolean supports(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        return path.matches("^/api/v1/composition/recruitments/[0-9]+$");
    }

    @Override
    public Mono<String> handle(ServerWebExchange exchange) {
        String id = exchange.getRequest().getURI().getPath()
                .replace("/api/v1/composition/recruitments/", "");
        String token = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        Mono<String> requestMono = webClientBuilder.build()
                .get().uri("http://localhost:8083/api/v1/job-service/recruitment-requests/" + id)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve().bodyToMono(String.class);

        // requester info
        Mono<String> requesterMono = requestMono.flatMap(body -> {
            try {
                JsonNode requestJson = objectMapper.readTree(body);
                JsonNode dataNode = requestJson.get("data");
                if (dataNode != null && dataNode.has("requesterId")) {
                    String requesterId = dataNode.get("requesterId").asText();
                    if (requesterId != null && !requesterId.isBlank()) {
                        return webClientBuilder.build()
                                .get().uri("http://localhost:8082/api/v1/user-service/users/" + requesterId)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .retrieve().bodyToMono(String.class);
                    }
                }
                return Mono.just("null");
            } catch (Exception e) {
                return Mono.just("null");
            }
        });

        // department info
        Mono<String> departmentMono = requestMono.flatMap(body -> {
            try {
                JsonNode requestJson = objectMapper.readTree(body);
                JsonNode dataNode = requestJson.get("data");
                if (dataNode != null && dataNode.has("departmentId")) {
                    String departmentId = dataNode.get("departmentId").asText();
                    if (departmentId != null && !departmentId.isBlank()) {
                        return webClientBuilder.build()
                                .get().uri("http://localhost:8082/api/v1/user-service/departments/" + departmentId)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .retrieve().bodyToMono(String.class);
                    }
                }
                return Mono.just("null");
            } catch (Exception e) {
                return Mono.just("null");
            }
        });

        return Mono.zip(requestMono, requesterMono, departmentMono)
                .map(tuple -> {
                    try {
                        // Parse request JSON and extract data
                        JsonNode requestJson = objectMapper.readTree(tuple.getT1());
                        JsonNode requestData = requestJson.get("data");

                        // Parse requester JSON and extract data
                        JsonNode requesterJson = objectMapper.readTree(tuple.getT2());
                        JsonNode requesterData = requesterJson.get("data");

                        // Parse department JSON and extract data
                        JsonNode departmentJson = objectMapper.readTree(tuple.getT3());
                        JsonNode departmentData = departmentJson.get("data");

                        // Create new request object with requester and department embedded
                        JsonNode modifiedRequestData = requestData.deepCopy();
                        ((com.fasterxml.jackson.databind.node.ObjectNode) modifiedRequestData).set("requester",
                                requesterData);
                        ((com.fasterxml.jackson.databind.node.ObjectNode) modifiedRequestData).set("department",
                                departmentData);
                        ((com.fasterxml.jackson.databind.node.ObjectNode) modifiedRequestData).remove("requesterId");
                        ((com.fasterxml.jackson.databind.node.ObjectNode) modifiedRequestData).remove("departmentId");

                        return objectMapper.writeValueAsString(modifiedRequestData);
                    } catch (Exception e) {
                        return tuple.getT1();
                    }
                }).doOnSuccess(jsonBody -> {
                    // [SỬA LỖI CHÍNH]
                    // Thêm header UTF-8 vào response để client (trình duyệt) đọc tiếng Việt
                    exchange.getResponse().getHeaders()
                            .add(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
                });
    }

}
