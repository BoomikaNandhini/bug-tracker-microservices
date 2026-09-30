package com.bugtrackerpro.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private String message;

    @JsonProperty("isRead")
    private boolean isRead;

    private LocalDateTime createdAt;
}
