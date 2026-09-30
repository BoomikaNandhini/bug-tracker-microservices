package com.bugtrackerpro.dashboard.service;

import com.bugtrackerpro.dashboard.dto.DashboardDTO;
import com.bugtrackerpro.dashboard.feign.BugServiceClient;
import com.bugtrackerpro.dashboard.feign.ProjectServiceClient;
import com.bugtrackerpro.dashboard.feign.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final UserServiceClient userServiceClient;
    private final ProjectServiceClient projectServiceClient;
    private final BugServiceClient bugServiceClient;

    public DashboardDTO getDashboardStats(Long userId, String role) {
        log.info("Fetching dashboard stats for user {} with role {}", userId, role);
        Map<String, Object> stats = new LinkedHashMap<>();

        if (role == null) {
            return DashboardDTO.builder().stats(stats).build();
        }

        switch (role.toUpperCase()) {
            case "ADMIN":
                stats.put("totalUsers", getSafeCount(() -> userServiceClient.countUsers()));
                stats.put("totalRoles", 4); // Based on original Role enum (ADMIN, DEVELOPER, TESTER, MANAGER)
                break;
            case "MANAGER":
                stats.put("totalProjects", getSafeCount(() -> projectServiceClient.countProjects()));
                stats.put("totalBugs", getSafeCount(() -> bugServiceClient.countBugs()));
                stats.put("openBugs", getSafeCount(() -> bugServiceClient.countBugsByStatus("NOT_RESOLVED")));
                stats.put("closedBugs", getSafeCount(() -> bugServiceClient.countBugsByStatus("RESOLVED")));
                break;
            case "DEVELOPER":
                stats.put("myAssignedProjectsCount", getSafeCount(() -> projectServiceClient.countAssignedProjects(userId, "DEVELOPER")));
                stats.put("myAssignedBugsCount", getSafeCount(() -> bugServiceClient.countAssignedBugs(userId, "DEVELOPER")));
                break;
            case "TESTER":
                stats.put("myAssignedProjectsCount", getSafeCount(() -> projectServiceClient.countAssignedProjects(userId, "TESTER")));
                stats.put("myAssignedBugsCount", getSafeCount(() -> bugServiceClient.countAssignedBugs(userId, "TESTER")));
                stats.put("myRaisedBugsCount", getSafeCount(() -> bugServiceClient.countRaisedBugs(userId)));
                stats.put("closedBugsCount", getSafeCount(() -> bugServiceClient.countClosedRaisedBugs(userId)));
                break;
            default:
                log.warn("Unknown role: {}", role);
        }

        return DashboardDTO.builder().stats(stats).build();
    }

    private Long getSafeCount(java.util.function.Supplier<Long> call) {
        try {
            return call.get();
        } catch (Exception ex) {
            log.error("Failed to fetch count for dashboard", ex);
            return 0L;
        }
    }
}
