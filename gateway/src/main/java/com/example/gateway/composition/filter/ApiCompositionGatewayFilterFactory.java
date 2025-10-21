package com.example.gateway.composition.filter;

import com.example.gateway.composition.core.CompositionService;
import com.example.gateway.composition.core.CompositionServiceRegistry;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ApiCompositionGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final CompositionServiceRegistry compositionServiceRegistry;

    public ApiCompositionGatewayFilterFactory(CompositionServiceRegistry compositionServiceRegistry) {
        this.compositionServiceRegistry = compositionServiceRegistry;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            CompositionService service = compositionServiceRegistry.findMatching(path);

            if (service == null) {
                return chain.filter(exchange);
            }

            return service.compose(exchange)
                    .flatMap(result -> {
                        var response = exchange.getResponse();
                        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        response.getHeaders().set("Content-Type", "application/json; charset=UTF-8");
                        return response.writeWith(Mono.just(
                                response.bufferFactory().wrap(result.getBytes())));
                    });
        };
    }
}
