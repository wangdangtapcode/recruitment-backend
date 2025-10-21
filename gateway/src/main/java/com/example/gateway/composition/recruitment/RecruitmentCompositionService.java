package com.example.gateway.composition.recruitment;

import com.example.gateway.composition.core.CompositionHandler;
import com.example.gateway.composition.core.CompositionService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class RecruitmentCompositionService implements CompositionService {

    private final List<CompositionHandler> handlers;

    public RecruitmentCompositionService(List<CompositionHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public boolean supports(String path) {
        return path.startsWith("/api/v1/composition/recruitments");
    }

    @Override
    public Mono<String> compose(ServerWebExchange exchange) {
        return handlers.stream()
                .filter(h -> h.supports(exchange))
                .findFirst()
                .map(h -> h.handle(exchange))
                .orElse(Mono.just("{\"error\": \"No handler found\"}"));
    }
}
