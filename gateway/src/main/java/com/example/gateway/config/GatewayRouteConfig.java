package com.example.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.gateway.composition.filter.ApiCompositionGatewayFilterFactory;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
            ApiCompositionGatewayFilterFactory compositionFilter) {
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

                // Composition API Routes (apply composition filter)
                .route("composition-service", r -> r.path("/api/v1/composition/**")
                        .filters(f -> f.filter(compositionFilter.apply(new Object())))
                        .uri("no://op"))

                // Health check routes
                .route("health-check", r -> r.path("/actuator/health", "/health")
                        .uri("http://localhost:8081"))

                .build();
    }
}
