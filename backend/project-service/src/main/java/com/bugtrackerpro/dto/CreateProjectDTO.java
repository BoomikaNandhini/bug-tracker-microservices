package com.bugtrackerpro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectDTO {
    private String name;
    private String description;
    private List<Long> assignedDeveloperIds;
    private List<Long> assignedTesterIds;
}
