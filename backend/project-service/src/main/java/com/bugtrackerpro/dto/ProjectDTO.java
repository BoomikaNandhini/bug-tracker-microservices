package com.bugtrackerpro.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    
    // Instead of full UserDTOs, we return the IDs for now.
    // In Phase 7 (OpenFeign), we can fetch the real User details if needed!
    private Long createdById;
    private Set<Long> assignedDeveloperIds;
    private Set<Long> assignedTesterIds;
    
    // UI fields matching the old monolith structure
    private com.bugtrackerpro.feign.UserResponse createdBy;
    private Set<com.bugtrackerpro.feign.UserResponse> assignedDevelopers;
    private Set<com.bugtrackerpro.feign.UserResponse> assignedTesters;

    private LocalDateTime createdAt;
}
