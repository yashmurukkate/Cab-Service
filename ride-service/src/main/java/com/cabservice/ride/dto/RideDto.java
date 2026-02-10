package com.cabservice.ride.dto;

import com.cabservice.ride.entity.Ride.RideStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ride Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideDto {

    private Long id;
    private Long customerId;
    private Long driverId;
    private Long vehicleId;
    private String vehicleType;

    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupAddress;

    private Double dropoffLatitude;
    private Double dropoffLongitude;
    private String dropoffAddress;

    private RideStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime driverArrivedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    private BigDecimal estimatedFare;
    private BigDecimal actualFare;
    private BigDecimal distanceKm;
    private Integer durationMinutes;

    private BigDecimal customerRating;
    private BigDecimal driverRating;

    private String rideOtp;

    // Driver info (populated from Cab Service)
    private DriverInfo driver;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DriverInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String phone;
        private BigDecimal rating;
        private String vehicleNumber;
        private String vehicleModel;
        private String vehicleColor;
    }
}
