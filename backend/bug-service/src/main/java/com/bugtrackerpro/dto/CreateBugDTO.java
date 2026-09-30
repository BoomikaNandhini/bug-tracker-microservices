package com.bugtrackerpro.dto;

import com.bugtrackerpro.entity.BugPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBugDTO {
    private String title;
    private String description;
    private BugPriority priority;
    private Long projectId;
}
