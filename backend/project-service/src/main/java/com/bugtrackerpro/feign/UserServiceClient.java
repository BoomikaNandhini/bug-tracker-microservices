package com.bugtrackerpro.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service",
    fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    @GetMapping("/internal/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);
}
