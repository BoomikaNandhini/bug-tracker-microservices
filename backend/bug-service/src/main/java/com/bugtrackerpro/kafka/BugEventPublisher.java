package com.bugtrackerpro.kafka;

import com.bugtrackerpro.event.BugCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@ConditionalOnProperty(name = "notification.kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class BugEventPublisher {

    public static final String BUG_EVENTS_TOPIC = "bug-events";

    private final KafkaTemplate<String, BugCreatedEvent> kafkaTemplate;

    public void publishBugCreated(BugCreatedEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }

        CompletableFuture<SendResult<String, BugCreatedEvent>> future =
                kafkaTemplate.send(BUG_EVENTS_TOPIC, event.getBugId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                // Do not fail the already-created bug because notification delivery is asynchronous.
                log.error("Failed to publish BUG_CREATED event for bugId={}", event.getBugId(), ex);
            } else {
                log.info("Published BUG_CREATED event. bugId={}, partition={}, offset={}",
                        event.getBugId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
