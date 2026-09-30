package com.bugtrackerpro.service;

import org.springframework.stereotype.Service;

import com.bugtrackerpro.feign.NotificationRequest;
import com.bugtrackerpro.feign.NotificationServiceClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@RequiredArgsConstructor 
@Slf4j 
public class NotificationClientService {
    
    private final NotificationServiceClient notificationServiceClient;

    @CircuitBreaker(
            name = "NotificationServiceClientcreateNotification",
            fallbackMethod = "createNotificationFallback"
    )
    public void createNotification(NotificationRequest request) {

        notificationServiceClient.createNotification(request);
    }

    private void createNotificationFallback(
            NotificationRequest request,
            Throwable throwable) {

        log.warn(
                "Notification Service is unavailable. " +
                "Notification was not sent. recipientId={}, reason={}",
                request.getRecipientId(),
                throwable.getMessage()
        );
    }
}
