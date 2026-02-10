package com.cabservice.notification.dto;

import com.cabservice.notification.entity.Notification.NotificationChannel;
import com.cabservice.notification.entity.Notification.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

/**
 * Notification Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    @NotNull
    private Long userId;

    @NotNull
    private NotificationType type;

    @NotNull
    private NotificationChannel channel;

    @NotBlank
    private String recipient;

    private String subject;

    private String templateName;

    private Map<String, Object> templateData;

    private String content;
}
