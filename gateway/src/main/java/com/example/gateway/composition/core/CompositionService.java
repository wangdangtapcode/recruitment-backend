package com.example.gateway.composition.core;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface CompositionService {
    boolean supports(String path);

    Mono<String> compose(ServerWebExchange exchange);
}
