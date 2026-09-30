package com.bugtrackerpro.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BugCreatedEvent {
    private String eventId;
    private String eventType;
    private Long bugId;
    private Long projectId;
    private String projectName;
    private String bugTitle;
    private String bugDescription;
    private String createdAt;
    private List<NotificationRecipient> recipients;
}
