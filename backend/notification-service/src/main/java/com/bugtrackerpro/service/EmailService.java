package com.bugtrackerpro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notification.email.from:${spring.mail.username:}}")
    private String fromAddress;

    public void sendBugCreatedEmail(String to, String recipientName, String projectName, String messageText) {
        if (!emailEnabled || to == null || to.isBlank()) {
            return;
        }

        try {
            if (mailSender == null) {
                System.out.println("INFO: Email is enabled but JavaMailSender is not available.");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            message.setTo(to);
            message.setSubject("BugTracker Pro - New Notification");
            message.setText(String.format(
                    "Hello %s,%n%n%s%n%nPlease log in to BugTracker Pro for details.",
                    recipientName != null ? recipientName : "User",
                    messageText));

            mailSender.send(message);
        } catch (Exception ex) {
            // Email failure must not prevent the in-app notification from being stored.
            System.err.printf("Failed to send notification email to %s: %s%n", to, ex.getMessage());
        }
    }
}
