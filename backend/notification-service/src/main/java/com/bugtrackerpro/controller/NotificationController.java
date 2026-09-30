package com.bugtrackerpro.controller;

import com.bugtrackerpro.dto.NotificationDTO;
import com.bugtrackerpro.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * The API Gateway validates the JWT and injects the trusted X-User-Id header.
     */
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(notificationService.getMyNotifications(requireUserId(userId)));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(notificationService.getMyUnreadNotifications(requireUserId(userId)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        notificationService.markAsRead(id, requireUserId(userId));
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        notificationService.markAllAsRead(requireUserId(userId));
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user identity is required");
        }
        return userId;
    }
}
