package com.example.gateway.composition.core;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class CompositionServiceRegistry {

    private final List<CompositionService> compositionServices;

    public CompositionServiceRegistry(List<CompositionService> compositionServices) {
        this.compositionServices = compositionServices;
    }

    public CompositionService findMatching(String path) {
        return compositionServices.stream()
                .filter(service -> service.supports(path))
                .findFirst()
                .orElse(null);
    }
}
