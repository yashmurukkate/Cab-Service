package com.cabservice.cab.mapper;

import com.cabservice.cab.dto.DriverDto;
import com.cabservice.cab.entity.Driver;
import com.cabservice.cab.entity.Vehicle;
import org.springframework.stereotype.Component;

/**
 * Driver Mapper - Converts entities to DTOs
 */
@Component
public class DriverMapper {

    public DriverDto toDriverDto(Driver driver) {
        if (driver == null) return null;

        DriverDto.DriverDtoBuilder builder = DriverDto.builder()
                .id(driver.getId())
                .userId(driver.getUserId())
                .licenseNumber(driver.getLicenseNumber())
                .licenseExpiry(driver.getLicenseExpiry())
                .status(driver.getStatus())
                .currentLatitude(driver.getCurrentLatitude())
                .currentLongitude(driver.getCurrentLongitude())
                .lastLocationUpdate(driver.getLastLocationUpdate())
                .rating(driver.getRating())
                .totalTrips(driver.getTotalTrips())
                .totalEarnings(driver.getTotalEarnings())
                .verified(driver.isVerified())
                .createdAt(driver.getCreatedAt());

        if (driver.getVehicle() != null) {
            builder.vehicle(toVehicleDto(driver.getVehicle()));
        }

        return builder.build();
    }

    public DriverDto.VehicleDto toVehicleDto(Vehicle vehicle) {
        if (vehicle == null) return null;

        return DriverDto.VehicleDto.builder()
                .id(vehicle.getId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .vehicleType(vehicle.getVehicleType())
                .vehicleModel(vehicle.getVehicleModel())
                .vehicleColor(vehicle.getVehicleColor())
                .capacity(vehicle.getCapacity())
                .registrationNumber(vehicle.getRegistrationNumber())
                .insuranceExpiry(vehicle.getInsuranceExpiry())
                .active(vehicle.isActive())
                .build();
    }
}
