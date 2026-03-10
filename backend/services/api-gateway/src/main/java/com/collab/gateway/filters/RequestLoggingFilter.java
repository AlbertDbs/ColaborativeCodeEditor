package com.collab.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        String id = exchange.getRequest().getId();
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name()
                : "UNKNOWN";

        return chain.filter(exchange)
                .doOnSuccess(unused -> log.info("[{}] {} {} -> {} in {}ms",
                        id, method, path, exchange.getResponse().getStatusCode(), System.currentTimeMillis() - start))
                .doOnError(err -> log.error("[{}] {} {} failed: {}", id, method, path, err.getMessage()));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
