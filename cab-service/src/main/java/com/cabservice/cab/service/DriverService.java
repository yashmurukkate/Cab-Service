package com.cabservice.cab.service;

import com.cabservice.cab.dto.*;
import com.cabservice.cab.entity.Driver;
import com.cabservice.cab.entity.DriverLocation;
import com.cabservice.cab.entity.Vehicle;
import com.cabservice.cab.exception.ResourceNotFoundException;
import com.cabservice.cab.mapper.DriverMapper;
import com.cabservice.cab.repository.DriverLocationRepository;
import com.cabservice.cab.repository.DriverRepository;
import com.cabservice.cab.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Driver Service - Handles driver and vehicle management
 */
@Service
@Transactional
public class DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverLocationRepository driverLocationRepository;
    private final DriverMapper driverMapper;

    public DriverService(DriverRepository driverRepository,
                         VehicleRepository vehicleRepository,
                         DriverLocationRepository driverLocationRepository,
                         DriverMapper driverMapper) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverLocationRepository = driverLocationRepository;
        this.driverMapper = driverMapper;
    }

    /**
     * Register a new driver with vehicle
     */
    public DriverDto registerDriver(DriverRegistrationRequest request) {
        logger.info("Registering new driver for user: {}", request.getUserId());

        // Check for existing driver
        if (driverRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new IllegalArgumentException("Driver already registered for this user");
        }

        // Check for duplicate license number
        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new IllegalArgumentException("License number already registered");
        }

        // Check for duplicate vehicle number
        if (vehicleRepository.existsByVehicleNumber(request.getVehicleNumber())) {
            throw new IllegalArgumentException("Vehicle number already registered");
        }

        // Create driver
        Driver driver = Driver.builder()
                .userId(request.getUserId())
                .licenseNumber(request.getLicenseNumber())
                .licenseExpiry(request.getLicenseExpiry())
                .status(Driver.DriverStatus.OFFLINE)
                .rating(BigDecimal.valueOf(5.0))
                .totalTrips(0)
                .totalEarnings(BigDecimal.ZERO)
                .verified(false)
                .build();

        driver = driverRepository.save(driver);

        // Create vehicle
        Vehicle vehicle = Vehicle.builder()
                .driver(driver)
                .vehicleNumber(request.getVehicleNumber())
                .vehicleType(request.getVehicleType())
                .vehicleModel(request.getVehicleModel())
                .vehicleColor(request.getVehicleColor())
                .capacity(request.getCapacity())
                .registrationNumber(request.getRegistrationNumber())
                .insuranceExpiry(request.getInsuranceExpiry())
                .active(true)
                .build();

        vehicleRepository.save(vehicle);
        driver.setVehicle(vehicle);

        logger.info("Driver registered successfully: {}", driver.getId());
        return driverMapper.toDriverDto(driver);
    }

    /**
     * Get driver by ID
     */
    public DriverDto getDriverById(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id));
        return driverMapper.toDriverDto(driver);
    }

    /**
     * Get driver by user ID
     */
    public DriverDto getDriverByUserId(Long userId) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "userId", userId));
        return driverMapper.toDriverDto(driver);
    }

    /**
     * Update driver status
     */
    public DriverDto updateDriverStatus(Long id, StatusUpdateRequest request) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id));

        driver.setStatus(request.getStatus());
        driver = driverRepository.save(driver);

        logger.info("Driver {} status updated to: {}", id, request.getStatus());
        return driverMapper.toDriverDto(driver);
    }

    /**
     * Update driver location
     */
    public void updateDriverLocation(Long id, LocationUpdateRequest request) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id));

        // Update current location
        driver.setCurrentLatitude(request.getLatitude());
        driver.setCurrentLongitude(request.getLongitude());
        driver.setLastLocationUpdate(LocalDateTime.now());
        driverRepository.save(driver);

        // Store location history
        DriverLocation location = DriverLocation.builder()
                .driverId(id)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .speed(request.getSpeed() != null ? request.getSpeed() : 0.0)
                .heading(request.getHeading())
                .build();
        driverLocationRepository.save(location);

        logger.debug("Driver {} location updated: {}, {}", id, request.getLatitude(), request.getLongitude());
    }

    /**
     * Get driver current location
     */
    public LocationUpdateRequest getDriverLocation(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id));

        return LocationUpdateRequest.builder()
                .latitude(driver.getCurrentLatitude())
                .longitude(driver.getCurrentLongitude())
                .build();
    }

    /**
     * Find nearby available cabs
     */
    public List<DriverDto> findNearbyCabs(Double latitude, Double longitude, 
                                          Double radiusKm, String vehicleType) {
        if (radiusKm == null) radiusKm = 5.0; // Default 5km radius

        List<Driver> drivers = driverRepository.findNearbyAvailableDrivers(
                latitude, longitude, radiusKm, vehicleType);

        return drivers.stream()
                .map(driverMapper::toDriverDto)
                .collect(Collectors.toList());
    }

    /**
     * Find nearest available driver
     */
    public DriverDto findNearestDriver(Double latitude, Double longitude, String vehicleType) {
        Driver driver = driverRepository.findNearestAvailableDriver(latitude, longitude, vehicleType)
                .orElse(null);
        return driver != null ? driverMapper.toDriverDto(driver) : null;
    }

    /**
     * Get driver rating
     */
    public BigDecimal getDriverRating(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id));
        return driver.getRating() != null ? driver.getRating() : BigDecimal.valueOf(5.0);
    }

    /**
     * Update driver rating
     */
    public void updateDriverRating(Long id, BigDecimal newRating) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id));

        BigDecimal currentRating = driver.getRating() != null ? driver.getRating() : BigDecimal.valueOf(5.0);
        int totalTrips = driver.getTotalTrips() != null ? driver.getTotalTrips() : 0;

        // Calculate new weighted average
        BigDecimal totalPoints = currentRating.multiply(BigDecimal.valueOf(totalTrips)).add(newRating);
        driver.setTotalTrips(totalTrips + 1);
        driver.setRating(totalPoints.divide(BigDecimal.valueOf(driver.getTotalTrips()), 2, RoundingMode.HALF_UP));

        driverRepository.save(driver);
        logger.info("Driver {} rating updated to: {}", id, driver.getRating());
    }

    /**
     * Get driver location history
     */
    public List<DriverLocation> getLocationHistory(Long driverId, int limit) {
        return driverLocationRepository.findByDriverIdOrderByTimestampDesc(
                driverId, PageRequest.of(0, limit));
    }
}
