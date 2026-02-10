package com.cabservice.ride.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Ride Booking Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideBookingRequest {

    @NotNull(message = "Pickup latitude is required")
    private Double pickupLatitude;

    @NotNull(message = "Pickup longitude is required")
    private Double pickupLongitude;

    private String pickupAddress;

    @NotNull(message = "Dropoff latitude is required")
    private Double dropoffLatitude;

    @NotNull(message = "Dropoff longitude is required")
    private Double dropoffLongitude;

    private String dropoffAddress;

    @NotNull(message = "Vehicle type is required")
    private String vehicleType;
}
