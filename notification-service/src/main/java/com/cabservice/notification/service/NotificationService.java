package com.cabservice.notification.service;

import com.cabservice.notification.dto.NotificationRequest;
import com.cabservice.notification.entity.Notification;
import com.cabservice.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Notification Service - Orchestrates all notification channels
 */
@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final PushNotificationService pushNotificationService;

    public NotificationService(NotificationRepository notificationRepository,
                               EmailService emailService,
                               SmsService smsService,
                               PushNotificationService pushNotificationService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.smsService = smsService;
        this.pushNotificationService = pushNotificationService;
    }

    /**
     * Send notification based on channel
     */
    @Async
    public void sendNotification(NotificationRequest request) {
        logger.info("Sending {} notification to user {} via {}",
                request.getType(), request.getUserId(), request.getChannel());

        // Create notification record
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .channel(request.getChannel())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .content(request.getContent())
                .status(Notification.NotificationStatus.PENDING)
                .retryCount(0)
                .build();

        notification = notificationRepository.save(notification);

        try {
            switch (request.getChannel()) {
                case EMAIL -> {
                    if (request.getTemplateName() != null) {
                        emailService.sendTemplatedEmail(
                                request.getRecipient(),
                                request.getSubject(),
                                request.getTemplateName(),
                                request.getTemplateData()
                        );
                    } else {
                        emailService.sendEmail(
                                request.getRecipient(),
                                request.getSubject(),
                                request.getContent()
                        );
                    }
                }
                case SMS -> smsService.sendSms(request.getRecipient(), request.getContent());
                case PUSH -> pushNotificationService.sendPushNotification(
                        request.getUserId(),
                        request.getSubject(),
                        request.getContent(),
                        request.getTemplateData()
                );
            }

            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            logger.info("Notification sent successfully: {}", notification.getId());

        } catch (Exception e) {
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setFailureReason(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
            logger.error("Failed to send notification: {}", e.getMessage());
        }

        notificationRepository.save(notification);
    }

    /**
     * Get user notification history
     */
    public Page<Notification> getUserNotifications(Long userId, int page, int size) {
        return notificationRepository.findByUserId(userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    /**
     * Retry failed notifications
     */
    public void retryFailedNotifications() {
        var failedNotifications = notificationRepository
                .findByStatusAndRetryCountLessThan(Notification.NotificationStatus.FAILED, 3);

        for (Notification notification : failedNotifications) {
            notification.setStatus(Notification.NotificationStatus.RETRY);
            notificationRepository.save(notification);

            // Convert to request and resend
            NotificationRequest request = NotificationRequest.builder()
                    .userId(notification.getUserId())
                    .type(notification.getType())
                    .channel(notification.getChannel())
                    .recipient(notification.getRecipient())
                    .subject(notification.getSubject())
                    .content(notification.getContent())
                    .build();

            sendNotification(request);
        }
    }
}
