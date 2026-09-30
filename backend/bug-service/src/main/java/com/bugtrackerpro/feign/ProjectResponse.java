package com.bugtrackerpro.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Mirrors the ProjectDTO from project-service.
 * Bug Service uses this when fetching project info via Feign.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private Long createdById;
    private Set<Long> assignedDeveloperIds;
    private Set<Long> assignedTesterIds;
    private LocalDateTime createdAt;
}
