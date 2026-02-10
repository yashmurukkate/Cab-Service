package com.cabservice.ride.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ride Entity - Represents a ride booking
 */
@Entity
@Table(name = "rides", indexes = {
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_driver_id", columnList = "driver_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "vehicle_type", nullable = false)
    private String vehicleType;

    // Pickup location
    @Column(name = "pickup_latitude", nullable = false)
    private Double pickupLatitude;

    @Column(name = "pickup_longitude", nullable = false)
    private Double pickupLongitude;

    @Column(name = "pickup_address")
    private String pickupAddress;

    // Dropoff location
    @Column(name = "dropoff_latitude", nullable = false)
    private Double dropoffLatitude;

    @Column(name = "dropoff_longitude", nullable = false)
    private Double dropoffLongitude;

    @Column(name = "dropoff_address")
    private String dropoffAddress;

    // Ride status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status;

    // Timestamps
    @CreationTimestamp
    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "driver_arrived_at")
    private LocalDateTime driverArrivedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "cancelled_by")
    private String cancelledBy;

    // Fare details
    @Column(name = "estimated_fare", precision = 10, scale = 2)
    private BigDecimal estimatedFare;

    @Column(name = "actual_fare", precision = 10, scale = 2)
    private BigDecimal actualFare;

    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    // Rating and feedback
    @Column(name = "customer_rating", precision = 2, scale = 1)
    private BigDecimal customerRating;

    @Column(name = "driver_rating", precision = 2, scale = 1)
    private BigDecimal driverRating;

    @Column(name = "customer_feedback")
    private String customerFeedback;

    @Column(name = "driver_feedback")
    private String driverFeedback;

    // OTP for ride verification
    @Column(name = "ride_otp")
    private String rideOtp;

    public enum RideStatus {
        REQUESTED,
        SEARCHING_DRIVER,
        ACCEPTED,
        DRIVER_ARRIVED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
