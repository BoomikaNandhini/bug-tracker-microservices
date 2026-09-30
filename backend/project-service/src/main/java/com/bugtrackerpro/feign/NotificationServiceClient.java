package com.bugtrackerpro.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service",
             fallback = NotificationServiceClientFallback.class
)
public interface NotificationServiceClient {

    @PostMapping("/internal/notifications")
    void createNotification(@RequestBody NotificationRequest request);
}
