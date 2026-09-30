package com.bugtrackerpro.dto;

import com.bugtrackerpro.entity.BugStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BugHistoryDTO {
    private Long id;
    private BugStatus status;
    private String changedByName;
    private String changedByRole;
    private LocalDateTime timestamp;
    private String description;
    private List<BugAttachmentDTO> attachments;
}
