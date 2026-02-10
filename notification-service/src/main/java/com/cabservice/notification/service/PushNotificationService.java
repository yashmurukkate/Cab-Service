package com.cabservice.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Push Notification Service - Mock push notifications
 */
@Service
public class PushNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

    /**
     * Send push notification (mock implementation)
     */
    public void sendPushNotification(Long userId, String title, String body, Map<String, Object> data) {
        // Mock push notification - In production, integrate with Firebase Cloud Messaging, APNs, etc.
        logger.info("Push notification sent to user {}: title={}, body={}", userId, title, body);
    }

    public void sendRideRequestNotification(Long driverId, Long rideId, String pickupAddress) {
        sendPushNotification(driverId, "New Ride Request",
                "New ride request from " + pickupAddress,
                Map.of("type", "RIDE_REQUEST", "rideId", rideId));
    }

    public void sendDriverAssignedNotification(Long customerId, String driverName, String eta) {
        sendPushNotification(customerId, "Driver Assigned",
                String.format("Driver %s is on the way. ETA: %s", driverName, eta),
                Map.of("type", "DRIVER_ASSIGNED"));
    }

    public void sendDriverArrivedNotification(Long customerId, String driverName) {
        sendPushNotification(customerId, "Driver Arrived",
                String.format("Driver %s has arrived at pickup location", driverName),
                Map.of("type", "DRIVER_ARRIVED"));
    }

    public void sendRideStartedNotification(Long customerId) {
        sendPushNotification(customerId, "Ride Started",
                "Your ride has started. Have a safe journey!",
                Map.of("type", "RIDE_STARTED"));
    }

    public void sendRideCompletedNotification(Long customerId, String fare) {
        sendPushNotification(customerId, "Ride Completed",
                String.format("Your ride is complete. Total: %s", fare),
                Map.of("type", "RIDE_COMPLETED"));
    }
}
