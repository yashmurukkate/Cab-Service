package com.cabservice.user.kafka;

import com.cabservice.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Producer for User Events
 */
@Component
public class UserEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.user-events:user-events}")
    private String userEventsTopic;

    public UserEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send user registered event
     */
    public void sendUserRegisteredEvent(User user) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "USER_REGISTERED");
        event.put("userId", user.getId());
        event.put("email", user.getEmail());
        event.put("phone", user.getPhone());
        event.put("firstName", user.getFirstName());
        event.put("lastName", user.getLastName());
        event.put("role", user.getRole().name());
        event.put("timestamp", System.currentTimeMillis());

        sendEvent(event);
        logger.info("User registered event sent for user: {}", user.getId());
    }

    /**
     * Send user updated event
     */
    public void sendUserUpdatedEvent(User user) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "USER_UPDATED");
        event.put("userId", user.getId());
        event.put("email", user.getEmail());
        event.put("firstName", user.getFirstName());
        event.put("lastName", user.getLastName());
        event.put("timestamp", System.currentTimeMillis());

        sendEvent(event);
        logger.info("User updated event sent for user: {}", user.getId());
    }

    /**
     * Send user status changed event
     */
    public void sendUserStatusChangedEvent(User user) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "USER_STATUS_CHANGED");
        event.put("userId", user.getId());
        event.put("status", user.getStatus().name());
        event.put("timestamp", System.currentTimeMillis());

        sendEvent(event);
        logger.info("User status changed event sent for user: {}", user.getId());
    }

    private void sendEvent(Map<String, Object> event) {
        try {
            kafkaTemplate.send(userEventsTopic, event.get("userId").toString(), event);
        } catch (Exception e) {
            logger.error("Failed to send user event: {}", e.getMessage());
        }
    }
}
