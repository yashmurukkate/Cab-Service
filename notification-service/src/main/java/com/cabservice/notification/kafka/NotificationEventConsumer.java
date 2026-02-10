package com.cabservice.notification.kafka;

import com.cabservice.notification.dto.NotificationRequest;
import com.cabservice.notification.entity.Notification;
import com.cabservice.notification.service.NotificationService;
import com.cabservice.notification.service.PushNotificationService;
import com.cabservice.notification.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka Consumer for handling events from other services
 */
@Component
public class NotificationEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;
    private final SmsService smsService;
    private final PushNotificationService pushNotificationService;

    public NotificationEventConsumer(NotificationService notificationService,
                                     SmsService smsService,
                                     PushNotificationService pushNotificationService) {
        this.notificationService = notificationService;
        this.smsService = smsService;
        this.pushNotificationService = pushNotificationService;
    }

    @KafkaListener(topics = "user-events", groupId = "notification-service-group")
    public void handleUserEvents(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        logger.info("Received user event: {}", eventType);

        try {
            switch (eventType) {
                case "USER_REGISTERED" -> handleUserRegistered(event);
                default -> logger.debug("Unhandled user event type: {}", eventType);
            }
        } catch (Exception e) {
            logger.error("Error processing user event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "ride-events", groupId = "notification-service-group")
    public void handleRideEvents(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        logger.info("Received ride event: {}", eventType);

        try {
            switch (eventType) {
                case "RIDE_REQUESTED" -> handleRideRequested(event);
                case "RIDE_ACCEPTED" -> handleRideAccepted(event);
                case "DRIVER_ARRIVED" -> handleDriverArrived(event);
                case "RIDE_STARTED" -> handleRideStarted(event);
                case "RIDE_COMPLETED" -> handleRideCompleted(event);
                case "RIDE_CANCELLED" -> handleRideCancelled(event);
                default -> logger.debug("Unhandled ride event type: {}", eventType);
            }
        } catch (Exception e) {
            logger.error("Error processing ride event: {}", e.getMessage());
        }
    }

    private void handleUserRegistered(Map<String, Object> event) {
        Long userId = Long.valueOf(event.get("userId").toString());
        String email = (String) event.get("email");
        String firstName = (String) event.get("firstName");

        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .type(Notification.NotificationType.EMAIL_VERIFICATION)
                .channel(Notification.NotificationChannel.EMAIL)
                .recipient(email)
                .subject("Welcome to CabService!")
                .content(String.format("Hi %s, welcome to CabService! Please verify your email to get started.", firstName))
                .build();

        notificationService.sendNotification(request);
    }

    private void handleRideRequested(Map<String, Object> event) {
        Long customerId = Long.valueOf(event.get("customerId").toString());
        Long rideId = Long.valueOf(event.get("rideId").toString());

        // Send push notification to customer
        pushNotificationService.sendPushNotification(customerId, "Ride Requested",
                "Looking for nearby drivers...", Map.of("type", "RIDE_REQUESTED", "rideId", rideId));
    }

    private void handleRideAccepted(Map<String, Object> event) {
        Long customerId = Long.valueOf(event.get("customerId").toString());
        pushNotificationService.sendDriverAssignedNotification(customerId, "Driver", "5 mins");
    }

    private void handleDriverArrived(Map<String, Object> event) {
        Long customerId = Long.valueOf(event.get("customerId").toString());
        pushNotificationService.sendDriverArrivedNotification(customerId, "Driver");
    }

    private void handleRideStarted(Map<String, Object> event) {
        Long customerId = Long.valueOf(event.get("customerId").toString());
        pushNotificationService.sendRideStartedNotification(customerId);
    }

    private void handleRideCompleted(Map<String, Object> event) {
        Long customerId = Long.valueOf(event.get("customerId").toString());
        Object fare = event.get("estimatedFare");
        pushNotificationService.sendRideCompletedNotification(customerId, fare != null ? fare.toString() : "N/A");
    }

    private void handleRideCancelled(Map<String, Object> event) {
        Long customerId = Long.valueOf(event.get("customerId").toString());
        pushNotificationService.sendPushNotification(customerId, "Ride Cancelled",
                "Your ride has been cancelled.", Map.of("type", "RIDE_CANCELLED"));
    }
}
