package com.bugtrackerpro.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@ConditionalOnProperty(name = "notification.kafka.enabled", havingValue = "true")
@EnableKafka
public class KafkaConsumerConfig {
    // Spring Boot's Kafka auto-configuration is used.
    // This class explicitly enables @KafkaListener processing.
}
