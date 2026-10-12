package com.shopsphere.gateway;

import com.shopsphere.common.security.JwtTokens;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) { SpringApplication.run(GatewayApplication.class, args); }

    @Bean JwtTokens jwtTokens(@Value("${shopsphere.jwt.secret}") String secret) {
        return new JwtTokens(secret, Duration.ofHours(8));
    }

    /** Rate-limit by client IP (first X-Forwarded-For hop when behind the ingress). */
    @Bean KeyResolver clientIpKeyResolver() {
        return exchange -> Mono.just(Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-Forwarded-For"))
                .map(value -> value.split(",")[0].trim())
                .orElseGet(() -> Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                        .map(InetSocketAddress::getHostString).orElse("unknown")));
    }
}
