package com.bugtrackerpro.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugAttachmentDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private String base64Data;
}
