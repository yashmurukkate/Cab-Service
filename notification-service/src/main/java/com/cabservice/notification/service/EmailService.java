package com.cabservice.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.util.Map;

/**
 * Email Service - Handles email notifications
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@cabservice.com}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendEmail(String to, String subject, String content) {
        if (!emailEnabled) {
            logger.info("Email disabled. Would send to: {}, subject: {}", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Async
    public void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> templateData) {
        Context context = new Context();
        templateData.forEach(context::setVariable);
        String content = templateEngine.process(templateName, context);
        sendEmail(to, subject, content);
    }

    public void sendVerificationEmail(String to, String token) {
        Map<String, Object> data = Map.of(
                "verificationUrl", "http://localhost:3000/verify-email?token=" + token
        );
        sendTemplatedEmail(to, "Verify Your Email - CabService", "email-verification", data);
    }

    public void sendPasswordResetEmail(String to, String token) {
        Map<String, Object> data = Map.of(
                "resetUrl", "http://localhost:3000/reset-password?token=" + token
        );
        sendTemplatedEmail(to, "Reset Your Password - CabService", "password-reset", data);
    }

    public void sendRideConfirmationEmail(String to, Map<String, Object> rideData) {
        sendTemplatedEmail(to, "Ride Confirmation - CabService", "ride-confirmation", rideData);
    }
}
