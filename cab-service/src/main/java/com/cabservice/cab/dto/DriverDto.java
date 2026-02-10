package com.cabservice.cab.dto;

import com.cabservice.cab.entity.Driver.DriverStatus;
import com.cabservice.cab.entity.Vehicle.VehicleType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Driver Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverDto {

    private Long id;
    private Long userId;
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private DriverStatus status;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime lastLocationUpdate;
    private BigDecimal rating;
    private Integer totalTrips;
    private BigDecimal totalEarnings;
    private boolean verified;
    private LocalDateTime createdAt;

    // Vehicle info
    private VehicleDto vehicle;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehicleDto {
        private Long id;
        private String vehicleNumber;
        private VehicleType vehicleType;
        private String vehicleModel;
        private String vehicleColor;
        private Integer capacity;
        private String registrationNumber;
        private LocalDate insuranceExpiry;
        private boolean active;
    }
}
