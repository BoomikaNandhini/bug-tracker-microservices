package com.bugtrackerpro.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDTO {
    private Long id;
    private Long bugId;
    private Long userId;
    private String userName;
    private String comment;
    private LocalDateTime createdAt;
}
