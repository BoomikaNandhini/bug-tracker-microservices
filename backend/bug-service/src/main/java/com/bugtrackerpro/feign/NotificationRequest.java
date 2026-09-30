package com.bugtrackerpro.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request sent from Bug Service to Notification Service.
 *
 * The notification service owns the Notification entity/database, so Bug Service
 * sends only the data required to create a notification.
 */
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
