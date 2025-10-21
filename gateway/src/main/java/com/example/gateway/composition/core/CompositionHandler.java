package com.example.gateway.composition.core;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface CompositionHandler {
    boolean supports(ServerWebExchange exchange);

    Mono<String> handle(ServerWebExchange exchange);
}
