package com.bugtrackerpro.config;

import com.bugtrackerpro.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "notification.kafka.enabled", havingValue = "true")
public class KafkaTopicConfig {

    @Bean
    public NewTopic bugEventsTopic() {
        return TopicBuilder.name(KafkaTopics.BUG_EVENTS)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
