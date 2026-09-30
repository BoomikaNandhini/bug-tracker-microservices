package com.bugtrackerpro.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Phase 9: Central JWT authentication at the API Gateway.
 *
 * The gateway validates the JWT once and propagates trusted identity headers
 * to downstream services. Client-supplied identity headers are removed first
 * so they cannot be spoofed.
 */
@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    private final String secret;

    public JwtGatewayFilter(@Value("${app.jwt.secret}") String secret) {
        this.secret = secret;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // CORS pre-flight and authentication endpoints do not require a JWT.
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequest().getMethod().name()) || isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authorization.substring(7).trim();
        if (token.isEmpty()) {
            return unauthorized(exchange, "Missing JWT token");
        }

        try {
            Claims claims = parseToken(token);

            Long userId = extractUserId(claims);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            if (userId == null || email == null || email.isBlank()) {
                return unauthorized(exchange, "JWT does not contain required user identity claims");
            }

            // Never trust identity headers supplied by the browser/client.
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .headers(headers -> {
                        headers.remove("X-User-Id");
                        headers.remove("X-User-Email");
                        headers.remove("X-User-Role");
                        headers.add("X-User-Id", String.valueOf(userId));
                        headers.add("X-User-Email", email);
                        if (role != null && !role.isBlank()) {
                            headers.add("X-User-Role", role);
                        }
                    })
                    .build();

            return chain.filter(exchange.mutate().request(request).build());
        } catch (Exception ex) {
            return unauthorized(exchange, "Invalid or expired JWT token");
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Long extractUserId(Claims claims) {
        Object value = claims.get("userId");
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.valueOf(text);
        }
        return null;
    }

    private boolean isPublicPath(String path) {
        return "/api/auth/login".equals(path)
                || "/api/auth/forgot-password".equals(path)
                || "/api/auth/verify-otp".equals(path)
                || "/api/auth/reset-password".equals(path);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        byte[] bytes = ("{\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
