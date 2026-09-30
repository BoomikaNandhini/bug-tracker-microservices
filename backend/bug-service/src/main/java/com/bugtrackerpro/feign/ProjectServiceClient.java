package com.bugtrackerpro.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * OpenFeign Client: Bug Service → Project Service
 *
 * "name" must match the spring.application.name of project-service.
 */
@FeignClient(name = "project-service",
    fallback = ProjectServiceClientFallback.class
)
public interface ProjectServiceClient {

    @GetMapping("/internal/projects/{id}")
    ProjectResponse getProjectById(@PathVariable("id") Long id);
}
