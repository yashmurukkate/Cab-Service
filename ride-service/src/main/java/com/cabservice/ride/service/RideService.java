package com.cabservice.ride.service;

import com.cabservice.ride.client.BillingServiceClient;
import com.cabservice.ride.client.CabServiceClient;
import com.cabservice.ride.client.RoutingServiceClient;
import com.cabservice.ride.dto.*;
import com.cabservice.ride.entity.Ride;
import com.cabservice.ride.entity.RideLocation;
import com.cabservice.ride.exception.ResourceNotFoundException;
import com.cabservice.ride.kafka.RideEventProducer;
import com.cabservice.ride.repository.RideLocationRepository;
import com.cabservice.ride.repository.RideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Ride Service - Handles ride booking and lifecycle management
 */
@Service
@Transactional
public class RideService {

    private static final Logger logger = LoggerFactory.getLogger(RideService.class);

    private final RideRepository rideRepository;
    private final RideLocationRepository rideLocationRepository;
    private final CabServiceClient cabServiceClient;
    private final BillingServiceClient billingServiceClient;
    private final RoutingServiceClient routingServiceClient;
    private final RideEventProducer rideEventProducer;

    public RideService(RideRepository rideRepository,
                       RideLocationRepository rideLocationRepository,
                       CabServiceClient cabServiceClient,
                       BillingServiceClient billingServiceClient,
                       RoutingServiceClient routingServiceClient,
                       RideEventProducer rideEventProducer) {
        this.rideRepository = rideRepository;
        this.rideLocationRepository = rideLocationRepository;
        this.cabServiceClient = cabServiceClient;
        this.billingServiceClient = billingServiceClient;
        this.routingServiceClient = routingServiceClient;
        this.rideEventProducer = rideEventProducer;
    }

    /**
     * Book a new ride
     */
    public RideDto bookRide(Long customerId, RideBookingRequest request) {
        logger.info("Booking ride for customer: {}", customerId);

        // Check if customer has an active ride
        if (rideRepository.findActiveRideByCustomerId(customerId).isPresent()) {
            throw new IllegalStateException("Customer already has an active ride");
        }

        // Calculate estimated fare
        BigDecimal estimatedFare = calculateEstimatedFare(request);

        // Generate ride OTP
        String rideOtp = String.format("%04d", new Random().nextInt(10000));

        // Create ride
        Ride ride = Ride.builder()
                .customerId(customerId)
                .vehicleType(request.getVehicleType())
                .pickupLatitude(request.getPickupLatitude())
                .pickupLongitude(request.getPickupLongitude())
                .pickupAddress(request.getPickupAddress())
                .dropoffLatitude(request.getDropoffLatitude())
                .dropoffLongitude(request.getDropoffLongitude())
                .dropoffAddress(request.getDropoffAddress())
                .status(Ride.RideStatus.SEARCHING_DRIVER)
                .estimatedFare(estimatedFare)
                .rideOtp(rideOtp)
                .build();

        ride = rideRepository.save(ride);

        // Publish ride requested event
        rideEventProducer.sendRideRequestedEvent(ride);

        logger.info("Ride booked successfully: {}", ride.getId());
        return toRideDto(ride);
    }

    /**
     * Accept ride by driver
     */
    public RideDto acceptRide(Long rideId, Long driverId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride", "id", rideId));

        if (ride.getStatus() != Ride.RideStatus.SEARCHING_DRIVER && 
            ride.getStatus() != Ride.RideStatus.REQUESTED) {
            throw new IllegalStateException("Ride cannot be accepted in current status: " + ride.getStatus());
        }

        // Check if driver already has an active ride
        if (rideRepository.findActiveRideByDriverId(driverId).isPresent()) {
            throw new IllegalStateException("Driver already has an active ride");
        }

        ride.setDriverId(driverId);
        ride.setStatus(Ride.RideStatus.ACCEPTED);
        ride.setAcceptedAt(LocalDateTime.now());

        ride = rideRepository.save(ride);

        // Update driver status to BUSY
        try {
            cabServiceClient.updateDriverStatus(driverId, Map.of("status", "BUSY"));
        } catch (Exception e) {
            logger.warn("Failed to update driver status: {}", e.getMessage());
        }

        rideEventProducer.sendRideAcceptedEvent(ride);

        logger.info("Ride {} accepted by driver {}", rideId, driverId);
        return toRideDto(ride);
    }

    /**
     * Driver arrived at pickup
     */
    public RideDto driverArrived(Long rideId, Long driverId) {
        Ride ride = getAndValidateRide(rideId, driverId, Ride.RideStatus.ACCEPTED);

        ride.setStatus(Ride.RideStatus.DRIVER_ARRIVED);
        ride.setDriverArrivedAt(LocalDateTime.now());
        ride = rideRepository.save(ride);

        rideEventProducer.sendDriverArrivedEvent(ride);

        logger.info("Driver arrived for ride: {}", rideId);
        return toRideDto(ride);
    }

    /**
     * Start the ride
     */
    public RideDto startRide(Long rideId, Long driverId, String otp) {
        Ride ride = getAndValidateRide(rideId, driverId, Ride.RideStatus.DRIVER_ARRIVED);

        // Verify OTP
        if (!ride.getRideOtp().equals(otp)) {
            throw new IllegalStateException("Invalid OTP");
        }

        ride.setStatus(Ride.RideStatus.IN_PROGRESS);
        ride.setStartedAt(LocalDateTime.now());
        ride = rideRepository.save(ride);

        rideEventProducer.sendRideStartedEvent(ride);

        logger.info("Ride started: {}", rideId);
        return toRideDto(ride);
    }

    /**
     * Complete the ride
     */
    public RideDto completeRide(Long rideId, Long driverId) {
        Ride ride = getAndValidateRide(rideId, driverId, Ride.RideStatus.IN_PROGRESS);

        // Calculate actual fare and distance
        BigDecimal distance = calculateActualDistance(ride);
        int duration = (int) java.time.Duration.between(ride.getStartedAt(), LocalDateTime.now()).toMinutes();

        ride.setStatus(Ride.RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());
        ride.setDistanceKm(distance);
        ride.setDurationMinutes(duration);
        ride.setActualFare(calculateActualFare(ride));

        ride = rideRepository.save(ride);

        // Update driver status to AVAILABLE
        try {
            cabServiceClient.updateDriverStatus(driverId, Map.of("status", "AVAILABLE"));
        } catch (Exception e) {
            logger.warn("Failed to update driver status: {}", e.getMessage());
        }

        // Generate invoice
        try {
            billingServiceClient.generateInvoice(Map.of(
                    "rideId", ride.getId(),
                    "customerId", ride.getCustomerId(),
                    "amount", ride.getActualFare()
            ));
        } catch (Exception e) {
            logger.warn("Failed to generate invoice: {}", e.getMessage());
        }

        rideEventProducer.sendRideCompletedEvent(ride);

        logger.info("Ride completed: {}", rideId);
        return toRideDto(ride);
    }

    /**
     * Cancel the ride
     */
    public RideDto cancelRide(Long rideId, Long userId, String reason, String cancelledBy) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride", "id", rideId));

        if (ride.getStatus() == Ride.RideStatus.COMPLETED || 
            ride.getStatus() == Ride.RideStatus.CANCELLED) {
            throw new IllegalStateException("Ride cannot be cancelled in current status");
        }

        ride.setStatus(Ride.RideStatus.CANCELLED);
        ride.setCancelledAt(LocalDateTime.now());
        ride.setCancellationReason(reason);
        ride.setCancelledBy(cancelledBy);

        ride = rideRepository.save(ride);

        // Update driver status if assigned
        if (ride.getDriverId() != null) {
            try {
                cabServiceClient.updateDriverStatus(ride.getDriverId(), Map.of("status", "AVAILABLE"));
            } catch (Exception e) {
                logger.warn("Failed to update driver status: {}", e.getMessage());
            }
        }

        rideEventProducer.sendRideCancelledEvent(ride);

        logger.info("Ride cancelled: {} by {}", rideId, cancelledBy);
        return toRideDto(ride);
    }

    /**
     * Rate the ride
     */
    public void rateRide(Long rideId, Long userId, RideRatingRequest request, boolean isCustomer) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride", "id", rideId));

        if (ride.getStatus() != Ride.RideStatus.COMPLETED) {
            throw new IllegalStateException("Can only rate completed rides");
        }

        if (isCustomer) {
            ride.setDriverRating(request.getRating());
            ride.setCustomerFeedback(request.getFeedback());
            // Update driver rating
            if (ride.getDriverId() != null) {
                try {
                    cabServiceClient.updateDriverRating(ride.getDriverId(), request.getRating());
                } catch (Exception e) {
                    logger.warn("Failed to update driver rating: {}", e.getMessage());
                }
            }
        } else {
            ride.setCustomerRating(request.getRating());
            ride.setDriverFeedback(request.getFeedback());
        }

        rideRepository.save(ride);
        logger.info("Ride {} rated by {}", rideId, isCustomer ? "customer" : "driver");
    }

    /**
     * Get ride by ID
     */
    public RideDto getRideById(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride", "id", rideId));
        return toRideDto(ride);
    }

    /**
     * Get customer ride history
     */
    public Page<RideDto> getCustomerRides(Long customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestedAt"));
        return rideRepository.findByCustomerId(customerId, pageable).map(this::toRideDto);
    }

    /**
     * Get driver ride history
     */
    public Page<RideDto> getDriverRides(Long driverId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestedAt"));
        return rideRepository.findByDriverId(driverId, pageable).map(this::toRideDto);
    }

    /**
     * Get all active rides (admin)
     */
    public List<RideDto> getActiveRides() {
        return rideRepository.findActiveRides().stream().map(this::toRideDto).toList();
    }

    /**
     * Update ride location during trip
     */
    public void updateRideLocation(Long rideId, Double latitude, Double longitude) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride", "id", rideId));

        if (ride.getStatus() != Ride.RideStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only track in-progress rides");
        }

        RideLocation location = RideLocation.builder()
                .rideId(rideId)
                .latitude(latitude)
                .longitude(longitude)
                .build();
        rideLocationRepository.save(location);
    }

    /**
     * Get ride track (location history)
     */
    public List<RideLocation> getRideTrack(Long rideId) {
        return rideLocationRepository.findByRideIdOrderByTimestampAsc(rideId);
    }

    // Helper methods
    private Ride getAndValidateRide(Long rideId, Long driverId, Ride.RideStatus expectedStatus) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride", "id", rideId));

        if (!ride.getDriverId().equals(driverId)) {
            throw new IllegalStateException("Driver not assigned to this ride");
        }

        if (ride.getStatus() != expectedStatus) {
            throw new IllegalStateException("Invalid ride status: " + ride.getStatus());
        }

        return ride;
    }

    private BigDecimal calculateEstimatedFare(RideBookingRequest request) {
        try {
            Map<String, Object> fareRequest = Map.of(
                    "pickupLatitude", request.getPickupLatitude(),
                    "pickupLongitude", request.getPickupLongitude(),
                    "dropoffLatitude", request.getDropoffLatitude(),
                    "dropoffLongitude", request.getDropoffLongitude(),
                    "vehicleType", request.getVehicleType()
            );
            Map<String, Object> response = billingServiceClient.calculateFare(fareRequest);
            return new BigDecimal(response.get("estimatedFare").toString());
        } catch (Exception e) {
            logger.warn("Failed to calculate fare from billing service: {}", e.getMessage());
            return BigDecimal.valueOf(100.00); // Default fare
        }
    }

    private BigDecimal calculateActualDistance(Ride ride) {
        try {
            Map<String, Object> request = Map.of(
                    "startLatitude", ride.getPickupLatitude(),
                    "startLongitude", ride.getPickupLongitude(),
                    "endLatitude", ride.getDropoffLatitude(),
                    "endLongitude", ride.getDropoffLongitude()
            );
            Map<String, Object> response = routingServiceClient.calculateDistance(request);
            return new BigDecimal(response.get("distanceKm").toString());
        } catch (Exception e) {
            logger.warn("Failed to calculate distance: {}", e.getMessage());
            return BigDecimal.valueOf(5.0); // Default distance
        }
    }

    private BigDecimal calculateActualFare(Ride ride) {
        // Base fare + per km rate
        BigDecimal baseFare = BigDecimal.valueOf(50);
        BigDecimal perKmRate = BigDecimal.valueOf(15);
        BigDecimal perMinRate = BigDecimal.valueOf(2);

        BigDecimal distanceCharge = ride.getDistanceKm().multiply(perKmRate);
        BigDecimal timeCharge = BigDecimal.valueOf(ride.getDurationMinutes()).multiply(perMinRate);

        return baseFare.add(distanceCharge).add(timeCharge);
    }

    private RideDto toRideDto(Ride ride) {
        return RideDto.builder()
                .id(ride.getId())
                .customerId(ride.getCustomerId())
                .driverId(ride.getDriverId())
                .vehicleId(ride.getVehicleId())
                .vehicleType(ride.getVehicleType())
                .pickupLatitude(ride.getPickupLatitude())
                .pickupLongitude(ride.getPickupLongitude())
                .pickupAddress(ride.getPickupAddress())
                .dropoffLatitude(ride.getDropoffLatitude())
                .dropoffLongitude(ride.getDropoffLongitude())
                .dropoffAddress(ride.getDropoffAddress())
                .status(ride.getStatus())
                .requestedAt(ride.getRequestedAt())
                .acceptedAt(ride.getAcceptedAt())
                .driverArrivedAt(ride.getDriverArrivedAt())
                .startedAt(ride.getStartedAt())
                .completedAt(ride.getCompletedAt())
                .cancelledAt(ride.getCancelledAt())
                .cancellationReason(ride.getCancellationReason())
                .estimatedFare(ride.getEstimatedFare())
                .actualFare(ride.getActualFare())
                .distanceKm(ride.getDistanceKm())
                .durationMinutes(ride.getDurationMinutes())
                .customerRating(ride.getCustomerRating())
                .driverRating(ride.getDriverRating())
                .rideOtp(ride.getRideOtp())
                .build();
    }
}
