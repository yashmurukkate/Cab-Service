package com.cabservice.cab.dto;

import com.cabservice.cab.entity.Vehicle.VehicleType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Driver Registration Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverRegistrationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotNull(message = "License expiry is required")
    @Future(message = "License must not be expired")
    private LocalDate licenseExpiry;

    // Vehicle details
    @NotBlank(message = "Vehicle number is required")
    private String vehicleNumber;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotBlank(message = "Vehicle model is required")
    private String vehicleModel;

    private String vehicleColor;

    @NotNull(message = "Vehicle capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 8, message = "Capacity cannot exceed 8")
    private Integer capacity;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    private LocalDate insuranceExpiry;
}
