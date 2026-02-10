package com.cabservice.ride.kafka;

import com.cabservice.ride.entity.Ride;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Producer for Ride Events
 */
@Component
public class RideEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(RideEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.ride-events:ride-events}")
    private String rideEventsTopic;

    public RideEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendRideRequestedEvent(Ride ride) {
        sendEvent("RIDE_REQUESTED", ride);
    }

    public void sendRideAcceptedEvent(Ride ride) {
        sendEvent("RIDE_ACCEPTED", ride);
    }

    public void sendDriverArrivedEvent(Ride ride) {
        sendEvent("DRIVER_ARRIVED", ride);
    }

    public void sendRideStartedEvent(Ride ride) {
        sendEvent("RIDE_STARTED", ride);
    }

    public void sendRideCompletedEvent(Ride ride) {
        sendEvent("RIDE_COMPLETED", ride);
    }

    public void sendRideCancelledEvent(Ride ride) {
        sendEvent("RIDE_CANCELLED", ride);
    }

    private void sendEvent(String eventType, Ride ride) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("rideId", ride.getId());
        event.put("customerId", ride.getCustomerId());
        event.put("driverId", ride.getDriverId());
        event.put("status", ride.getStatus().name());
        event.put("pickupLatitude", ride.getPickupLatitude());
        event.put("pickupLongitude", ride.getPickupLongitude());
        event.put("dropoffLatitude", ride.getDropoffLatitude());
        event.put("dropoffLongitude", ride.getDropoffLongitude());
        event.put("estimatedFare", ride.getEstimatedFare());
        event.put("timestamp", System.currentTimeMillis());

        try {
            kafkaTemplate.send(rideEventsTopic, ride.getId().toString(), event);
            logger.info("Ride event sent: {} for ride: {}", eventType, ride.getId());
        } catch (Exception e) {
            logger.error("Failed to send ride event: {}", e.getMessage());
        }
    }
}
