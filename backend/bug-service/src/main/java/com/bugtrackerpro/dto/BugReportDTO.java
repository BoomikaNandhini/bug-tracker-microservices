package com.bugtrackerpro.dto;

import com.bugtrackerpro.entity.BugPriority;
import com.bugtrackerpro.entity.BugStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BugReportDTO {
    private Long id;
    private String title;
    private String description;
    private BugStatus status;
    private BugPriority priority;

    // IDs of related entities
    private Long projectId;
    private String projectName;   // Fetched via Feign from Project Service
    private Long reportedById;
    private com.bugtrackerpro.feign.UserResponse reportedBy;
    
    private Long assignedToId;
    private com.bugtrackerpro.feign.UserResponse assignedTo;
    private List<Long> assignedDeveloperIds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BugAttachmentDTO> attachments;
}
