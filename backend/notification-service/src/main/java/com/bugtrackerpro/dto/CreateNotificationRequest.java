package com.bugtrackerpro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal request used by Bug Service to create an in-app notification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {
    private String eventId;
    private Long recipientId;
    private String recipientName;
    private String recipientEmail;
    private String message;
}
