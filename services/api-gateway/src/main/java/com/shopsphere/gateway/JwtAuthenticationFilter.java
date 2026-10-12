package com.shopsphere.gateway;

import com.shopsphere.common.security.JwtTokens;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Validates the bearer token once at the edge and forwards the caller's identity as X-User-Id /
 * X-User-Role. Any identity headers sent by the client are stripped first so they cannot be spoofed.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    static final String USER_HEADER = "X-User-Id";
    static final String ROLE_HEADER = "X-User-Role";
    private final JwtTokens tokens;

    public JwtAuthenticationFilter(JwtTokens tokens) { this.tokens = tokens; }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        AccessPolicy.Requirement requirement = AccessPolicy.requirementFor(request.getMethod(), request.getPath().value());
        Optional<JwtTokens.Principal> principal = bearer(request).flatMap(tokens::verify);

        if (requirement != AccessPolicy.Requirement.PUBLIC && principal.isEmpty()) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "Please sign in to continue");
        }
        if (requirement == AccessPolicy.Requirement.ADMIN && !"ADMIN".equals(principal.get().role())) {
            return reject(exchange, HttpStatus.FORBIDDEN, "Administrator role required");
        }
        ServerHttpRequest forwarded = request.mutate().headers(headers -> {
            headers.remove(USER_HEADER);
            headers.remove(ROLE_HEADER);
            principal.ifPresent(p -> {
                headers.set(USER_HEADER, p.userId());
                headers.set(ROLE_HEADER, p.role());
            });
        }).build();
        return chain.filter(exchange.mutate().request(forwarded).build());
    }

    @Override
    public int getOrder() { return Ordered.HIGHEST_PRECEDENCE + 10; }

    private static Optional<String> bearer(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return header != null && header.startsWith("Bearer ") ? Optional.of(header.substring(7).trim()) : Optional.empty();
    }

    private static Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"status\":" + status.value() + ",\"code\":\"" + status.name() + "\",\"message\":\"" + message + "\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
