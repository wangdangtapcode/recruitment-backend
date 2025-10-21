package com.example.gateway.composition.recruitment;

import com.example.gateway.composition.core.CompositionHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RecruitmentListHandler implements CompositionHandler {

    private final WebClient.Builder webClientBuilder;

    public RecruitmentListHandler(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public boolean supports(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        return path.equals("/api/v1/composition/recruitments");
    }

    @Override
    public Mono<String> handle(ServerWebExchange exchange) {
        Mono<String> requestsMono = webClientBuilder.build()
                .get().uri("http://localhost:8083/api/v1/job-service/recruitment-requests")
                .retrieve().bodyToMono(String.class);

        Mono<String> departmentsMono = webClientBuilder.build()
                .get().uri("http://localhost:8082/api/v1/user-service/departments")
                .retrieve().bodyToMono(String.class)
                .onErrorReturn("null");

        return Mono.zip(requestsMono, departmentsMono)
                .map(tuple -> """
                        {
                          "requests": %s,
                          "departments": %s
                        }
                        """.formatted(tuple.getT1(), tuple.getT2()));
    }
}
