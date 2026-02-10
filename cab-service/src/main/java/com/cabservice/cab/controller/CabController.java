package com.cabservice.cab.controller;

import com.cabservice.cab.dto.*;
import com.cabservice.cab.entity.Vehicle.VehicleType;
import com.cabservice.cab.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Cab Controller - Handles driver and vehicle endpoints
 */
@RestController
@RequestMapping("/api/cabs")
@Tag(name = "Cab Management", description = "Driver and vehicle management APIs")
public class CabController {

    private final DriverService driverService;

    public CabController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping("/drivers")
    @Operation(summary = "Register a new driver with vehicle")
    public ResponseEntity<DriverDto> registerDriver(
            @Valid @RequestBody DriverRegistrationRequest request) {
        DriverDto driver = driverService.registerDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(driver);
    }

    @GetMapping("/drivers/{id}")
    @Operation(summary = "Get driver by ID")
    public ResponseEntity<DriverDto> getDriverById(@PathVariable Long id) {
        DriverDto driver = driverService.getDriverById(id);
        return ResponseEntity.ok(driver);
    }

    @GetMapping("/drivers/user/{userId}")
    @Operation(summary = "Get driver by user ID")
    public ResponseEntity<DriverDto> getDriverByUserId(@PathVariable Long userId) {
        DriverDto driver = driverService.getDriverByUserId(userId);
        return ResponseEntity.ok(driver);
    }

    @PatchMapping("/drivers/{id}/status")
    @Operation(summary = "Update driver status")
    public ResponseEntity<DriverDto> updateDriverStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        DriverDto driver = driverService.updateDriverStatus(id, request);
        return ResponseEntity.ok(driver);
    }

    @PostMapping("/drivers/{id}/location")
    @Operation(summary = "Update driver location")
    public ResponseEntity<Map<String, String>> updateDriverLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationUpdateRequest request) {
        driverService.updateDriverLocation(id, request);
        return ResponseEntity.ok(Map.of("message", "Location updated successfully"));
    }

    @GetMapping("/drivers/{id}/location")
    @Operation(summary = "Get driver current location")
    public ResponseEntity<LocationUpdateRequest> getDriverLocation(@PathVariable Long id) {
        LocationUpdateRequest location = driverService.getDriverLocation(id);
        return ResponseEntity.ok(location);
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find nearby available cabs")
    public ResponseEntity<List<DriverDto>> findNearbyCabs(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false, defaultValue = "5.0") Double radius,
            @RequestParam(required = false) VehicleType vehicleType) {
        String vehicleTypeStr = vehicleType != null ? vehicleType.name() : null;
        List<DriverDto> drivers = driverService.findNearbyCabs(latitude, longitude, radius, vehicleTypeStr);
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/drivers/{id}/rating")
    @Operation(summary = "Get driver rating")
    public ResponseEntity<Map<String, BigDecimal>> getDriverRating(@PathVariable Long id) {
        BigDecimal rating = driverService.getDriverRating(id);
        return ResponseEntity.ok(Map.of("rating", rating));
    }

    @PostMapping("/drivers/{id}/rating")
    @Operation(summary = "Update driver rating")
    public ResponseEntity<Map<String, String>> updateDriverRating(
            @PathVariable Long id,
            @RequestParam BigDecimal rating) {
        driverService.updateDriverRating(id, rating);
        return ResponseEntity.ok(Map.of("message", "Rating updated successfully"));
    }
}
