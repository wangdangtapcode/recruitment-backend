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
                .build();
    }
}
