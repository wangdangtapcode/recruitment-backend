package com.example.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
                return builder.routes()
                                // User Service Routes
                                .route("user-service", r -> r.path("/api/v1/user-service/**")
                                                .uri("http://localhost:8082"))

                                // Job Service Routes
                                .route("job-service", r -> r.path("/api/v1/job-service/**")
                                                .uri("http://localhost:8083"))

                                // Candidate Service Routes
                                .route("candidate-service", r -> r.path("/api/v1/candidate-service/**")
                                                .uri("http://localhost:8084"))
                                // Communications Service Routes
                                .route("communications-service", r -> r.path("/api/v1/communications-service/**")
                                                .uri("http://localhost:8085"))
                                // Workflow Service Routes
                                .route("workflow-service", r -> r.path("/api/v1/workflow-service/**")
                                                .uri("http://localhost:8086"))
                                // Upload Service Routes
                                .route("upload-service", r -> r.path("/api/v1/upload/**")
                                                .uri("http://localhost:8087"))
                                // Notification Service Routes
                                .route("notification-service", r -> r.path("/api/v1/notification-service/**")
                                                .uri("http://localhost:8088"))
                                // --- Notification Service WebSocket ---
                                .route("notification-socketio", r -> r.path("/socket.io/**")
                                                .uri("http://localhost:9099"))
                                // Statistics Service Routes
                                .route("statistics-service", r -> r.path("/api/v1/statistics-service/**")
                                                .uri("http://localhost:8089"))
                                .build();
        }
}
