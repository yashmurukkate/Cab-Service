package com.cabservice.notification.controller;

import com.cabservice.notification.dto.NotificationRequest;
import com.cabservice.notification.entity.Notification;
import com.cabservice.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Notification Controller
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    @Operation(summary = "Send a notification")
    public ResponseEntity<Map<String, String>> sendNotification(
            @Valid @RequestBody NotificationRequest request) {
        notificationService.sendNotification(request);
        return ResponseEntity.ok(Map.of("message", "Notification queued for delivery"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user notification history")
    public ResponseEntity<Page<Notification>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Notification> notifications = notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/retry-failed")
    @Operation(summary = "Retry failed notifications")
    public ResponseEntity<Map<String, String>> retryFailedNotifications() {
        notificationService.retryFailedNotifications();
        return ResponseEntity.ok(Map.of("message", "Retry initiated for failed notifications"));
    }
}
