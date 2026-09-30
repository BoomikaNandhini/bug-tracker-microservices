package com.bugtrackerpro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        // Fallback for development: Log OTP to console in case SMTP is not configured
        System.out.printf("DEBUG: Sending Email to: %s%n", to);
        System.out.printf("DEBUG: Subject: %s%n", subject);
        System.out.printf("DEBUG: Body: %s%n", body);

        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("nandhinip1509@gmail.com");
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
            } else {
                System.out
                        .println("INFO: JavaMailSender is null. Ensure spring-boot-starter-mail is fully downloaded.");
            }
        } catch (Exception e) {
            System.err.printf("CRITICAL: Failed to send real email. Error: %s%n", e.getMessage());
        }
    }

    public void sendOtpEmail(String to, String otp) {
        String subject = "Password Reset OTP - BugTracker Pro";
        String body = String.format("Your OTP for password reset is: %s\n\nThis OTP is valid for 5 minutes.", otp);
        sendEmail(to, subject, body);
    }
}
