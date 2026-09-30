// package com.bugtrackerpro.service;

// import com.bugtrackerpro.event.BugCreatedEvent;
// import com.bugtrackerpro.event.NotificationRecipient;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Service;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

// @Service
// @ConditionalOnProperty(name = "notification.kafka.enabled", havingValue = "true")
// @RequiredArgsConstructor
// @Slf4j
// public class BugEventConsumer {

//     private final NotificationService notificationService;
//     private final EmailService emailService;

//     @KafkaListener(topics = "bug-events", groupId = "notification-service")
//     public void consumeBugCreated(BugCreatedEvent event) {
//         if (event == null || !"BUG_CREATED".equals(event.getEventType())) {
//             log.warn("Ignoring unsupported bug event: {}", event);
//             return;
//         }

//         log.info("Received BUG_CREATED event. eventId={}, bugId={}",
//                 event.getEventId(), event.getBugId());

//         if (event.getRecipients() == null) {
//             return;
//         }

//         String message = String.format(
//                 "A new bug '%s' has been created in project '%s'.",
//                 event.getBugTitle(),
//                 event.getProjectName());

//         for (NotificationRecipient recipient : event.getRecipients()) {
//             notificationService.createNotification(
//                     event.getEventId(),
//                     recipient.getUserId(),
//                     message);

//             emailService.sendBugCreatedEmail(
//                     recipient.getEmail(),
//                     recipient.getName(),
//                     event.getProjectName(),
//                     event.getBugTitle());
//         }
//     }
// }

package com.bugtrackerpro.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for bug events.
 *
 * Kafka is intentionally disabled during the initial Phase 8 migration.
 * This class is kept for the future Kafka implementation.
 *
 * To enable Kafka later:
 *
 * notification:
 *   kafka:
 *     enabled: true
 */
@Service
@Slf4j
@ConditionalOnProperty(
        name = "notification.kafka.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class BugEventConsumer {

    public BugEventConsumer() {
        log.info("BugEventConsumer initialized. Kafka event processing is enabled.");
    }

    // Kafka @KafkaListener implementation will be enabled in the Kafka phase.
}