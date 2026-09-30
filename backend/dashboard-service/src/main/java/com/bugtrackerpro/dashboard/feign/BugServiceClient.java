package com.bugtrackerpro.dashboard.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "bug-service")
public interface BugServiceClient {

    @GetMapping("/api/bugs/internal/count")
    Long countBugs();

    @GetMapping("/api/bugs/internal/count/status")
    Long countBugsByStatus(@RequestParam("status") String status);

    @GetMapping("/api/bugs/internal/assigned-count")
    Long countAssignedBugs(@RequestParam("userId") Long userId, @RequestParam("role") String role);

    @GetMapping("/api/bugs/internal/raised-count")
    Long countRaisedBugs(@RequestParam("userId") Long userId);

    @GetMapping("/api/bugs/internal/raised-closed-count")
    Long countClosedRaisedBugs(@RequestParam("userId") Long userId);
}
