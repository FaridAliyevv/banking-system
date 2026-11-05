package com.example.ms_api_gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements org.springframework.cloud.gateway.filter.GlobalFilter {

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        System.out.println("JWT FILTER PATH: " + path);

        String method = exchange.getRequest()
                .getMethod()
                .name();

        if (path.equals("/users/login") || path.equals("/users") && method.equals("POST")) {
            System.out.println("PUBLIC PATH - SKIPPING JWT: " + path);
            return chain.filter(exchange);
        }

        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/webjars")
                || path.endsWith("/v3/api-docs")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse()
                    .setStatusCode(HttpStatus.UNAUTHORIZED);

            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!jwtService.validateToken(token)) {
            exchange.getResponse()
                    .setStatusCode(HttpStatus.UNAUTHORIZED);

            return exchange.getResponse().setComplete();
        }

        String role = jwtService.extractRole(token);

        if (path.startsWith("/users")
                && exchange.getRequest().getMethod().matches("DELETE")
                && !role.equals("ADMIN")) {

            exchange.getResponse()
                    .setStatusCode(HttpStatus.FORBIDDEN);

            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}
