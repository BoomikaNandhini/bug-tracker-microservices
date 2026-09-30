package com.bugtrackerpro.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String eventId;
    private Long recipientId;
    private String recipientName;
    private String recipientEmail;
    private String message;
}
