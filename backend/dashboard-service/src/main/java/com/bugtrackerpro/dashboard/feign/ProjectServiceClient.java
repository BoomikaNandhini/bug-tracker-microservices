package com.bugtrackerpro.dashboard.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "project-service")
public interface ProjectServiceClient {

    @GetMapping("/api/projects/internal/count")
    Long countProjects();

    @GetMapping("/api/projects/internal/assigned-count")
    Long countAssignedProjects(@RequestParam("userId") Long userId, @RequestParam("role") String role);
}
