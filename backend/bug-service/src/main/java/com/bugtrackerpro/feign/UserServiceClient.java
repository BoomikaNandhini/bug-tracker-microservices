package com.bugtrackerpro.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * OpenFeign Client: Bug Service → User Service
 *
 * "name" must match the spring.application.name of user-service.
 * Eureka resolves "user-service" to the actual IP:PORT automatically.
 * No hardcoded URLs needed!
 */
@FeignClient(name = "user-service",
    fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    @GetMapping("/internal/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);
}
