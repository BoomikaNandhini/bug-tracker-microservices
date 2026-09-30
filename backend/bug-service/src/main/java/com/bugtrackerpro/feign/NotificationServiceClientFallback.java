package com.bugtrackerpro.feign;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component 
@Slf4j 
public class NotificationServiceClientFallback implements NotificationServiceClient{
    
    @Override
    public void createNotification(NotificationRequest request) {

        log.warn(
                "Notification Service is unavailable. " +
                "Notification could not be sent. recipientId={}, eventId={}",
                request.getRecipientId(),
                request.getEventId()
        );
    }
}
