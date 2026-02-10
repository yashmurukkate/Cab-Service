package com.cabservice.ride.controller;

import com.cabservice.ride.dto.*;
import com.cabservice.ride.entity.RideLocation;
import com.cabservice.ride.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Ride Controller - Handles ride booking and management endpoints
 */
@RestController
@RequestMapping("/api/rides")
@Tag(name = "Ride Management", description = "Ride booking and lifecycle management APIs")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping("/book")
    @Operation(summary = "Book a new ride")
    public ResponseEntity<RideDto> bookRide(
            @RequestHeader("X-User-Id") Long customerId,
            @Valid @RequestBody RideBookingRequest request) {
        RideDto ride = rideService.bookRide(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ride);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ride details")
    public ResponseEntity<RideDto> getRideById(@PathVariable Long id) {
        RideDto ride = rideService.getRideById(id);
        return ResponseEntity.ok(ride);
    }

    @PatchMapping("/{id}/accept")
    @Operation(summary = "Driver accepts the ride")
    public ResponseEntity<RideDto> acceptRide(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long driverId) {
        RideDto ride = rideService.acceptRide(id, driverId);
        return ResponseEntity.ok(ride);
    }

    @PatchMapping("/{id}/arrived")
    @Operation(summary = "Driver arrived at pickup")
    public ResponseEntity<RideDto> driverArrived(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long driverId) {
        RideDto ride = rideService.driverArrived(id, driverId);
        return ResponseEntity.ok(ride);
    }

    @PatchMapping("/{id}/start")
    @Operation(summary = "Start the ride")
    public ResponseEntity<RideDto> startRide(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long driverId,
            @RequestParam String otp) {
        RideDto ride = rideService.startRide(id, driverId, otp);
        return ResponseEntity.ok(ride);
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Complete the ride")
    public ResponseEntity<RideDto> completeRide(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long driverId) {
        RideDto ride = rideService.completeRide(id, driverId);
        return ResponseEntity.ok(ride);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel the ride")
    public ResponseEntity<RideDto> cancelRide(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String reason,
            @RequestParam String cancelledBy) {
        RideDto ride = rideService.cancelRide(id, userId, reason, cancelledBy);
        return ResponseEntity.ok(ride);
    }

    @PostMapping("/{id}/rate")
    @Operation(summary = "Rate the ride")
    public ResponseEntity<Map<String, String>> rateRide(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody RideRatingRequest request) {
        boolean isCustomer = "CUSTOMER".equals(role);
        rideService.rateRide(id, userId, request, isCustomer);
        return ResponseEntity.ok(Map.of("message", "Rating submitted successfully"));
    }

    @PostMapping("/{id}/location")
    @Operation(summary = "Update ride location during trip")
    public ResponseEntity<Map<String, String>> updateLocation(
            @PathVariable Long id,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        rideService.updateRideLocation(id, latitude, longitude);
        return ResponseEntity.ok(Map.of("message", "Location updated"));
    }

    @GetMapping("/{id}/track")
    @Operation(summary = "Get ride tracking data")
    public ResponseEntity<List<RideLocation>> getRideTrack(@PathVariable Long id) {
        List<RideLocation> track = rideService.getRideTrack(id);
        return ResponseEntity.ok(track);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer ride history")
    public ResponseEntity<Page<RideDto>> getCustomerRides(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<RideDto> rides = rideService.getCustomerRides(customerId, page, size);
        return ResponseEntity.ok(rides);
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get driver ride history")
    public ResponseEntity<Page<RideDto>> getDriverRides(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<RideDto> rides = rideService.getDriverRides(driverId, page, size);
        return ResponseEntity.ok(rides);
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active rides (admin)")
    public ResponseEntity<List<RideDto>> getActiveRides() {
        List<RideDto> rides = rideService.getActiveRides();
        return ResponseEntity.ok(rides);
    }
}
