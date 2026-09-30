package com.bugtrackerpro.service;

import com.bugtrackerpro.dto.NotificationDTO;
import com.bugtrackerpro.entity.Notification;
import com.bugtrackerpro.dto.CreateNotificationRequest;
import com.bugtrackerpro.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j 
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Transactional
    public void createNotification(CreateNotificationRequest request) {
        log.info("Inside create notification method");
        if (request == null || request.getRecipientId() == null
                || request.getMessage() == null || request.getMessage().isBlank()) {
            return;
        }

        // Keep the operation idempotent. This also makes the service ready for
        // the later Kafka implementation, where a message can be delivered more than once.
        if (request.getEventId() != null
                && notificationRepository.existsByEventIdAndRecipientId(
                        request.getEventId(), request.getRecipientId())) {
            return;
        }

        Notification notification = Notification.builder()
                .eventId(request.getEventId())
                .recipientId(request.getRecipientId())
                .message(request.getMessage())
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        // Email is optional and remains inside Notification Service.
        emailService.sendBugCreatedEmail(
                request.getRecipientEmail(),
                request.getRecipientName(),
                null,
                request.getMessage());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getMyNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getMyUnreadNotifications(Long userId) {
        return notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread =
                notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        unread.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
