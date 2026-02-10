package com.cabservice.cab.repository;

import com.cabservice.cab.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Driver Repository with geospatial queries
 */
@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByUserId(Long userId);

    boolean existsByLicenseNumber(String licenseNumber);

    List<Driver> findByStatus(Driver.DriverStatus status);

    /**
     * Find nearby available drivers using Haversine formula
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @param radiusKm Search radius in kilometers
     * @param vehicleType Optional vehicle type filter
     */
    @Query(value = """
        SELECT d.* FROM drivers d
        JOIN vehicles v ON d.id = v.driver_id
        WHERE d.status = 'AVAILABLE'
        AND d.current_latitude IS NOT NULL
        AND d.current_longitude IS NOT NULL
        AND (:vehicleType IS NULL OR v.vehicle_type = :vehicleType)
        AND (
            6371 * acos(
                cos(radians(:latitude)) * cos(radians(d.current_latitude)) *
                cos(radians(d.current_longitude) - radians(:longitude)) +
                sin(radians(:latitude)) * sin(radians(d.current_latitude))
            )
        ) <= :radiusKm
        ORDER BY (
            6371 * acos(
                cos(radians(:latitude)) * cos(radians(d.current_latitude)) *
                cos(radians(d.current_longitude) - radians(:longitude)) +
                sin(radians(:latitude)) * sin(radians(d.current_latitude))
            )
        ) ASC
        LIMIT 20
        """, nativeQuery = true)
    List<Driver> findNearbyAvailableDrivers(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm,
            @Param("vehicleType") String vehicleType
    );

    /**
     * Find nearest available driver
     */
    @Query(value = """
        SELECT d.* FROM drivers d
        JOIN vehicles v ON d.id = v.driver_id
        WHERE d.status = 'AVAILABLE'
        AND d.current_latitude IS NOT NULL
        AND d.current_longitude IS NOT NULL
        AND (:vehicleType IS NULL OR v.vehicle_type = :vehicleType)
        ORDER BY (
            6371 * acos(
                cos(radians(:latitude)) * cos(radians(d.current_latitude)) *
                cos(radians(d.current_longitude) - radians(:longitude)) +
                sin(radians(:latitude)) * sin(radians(d.current_latitude))
            )
        ) ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<Driver> findNearestAvailableDriver(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("vehicleType") String vehicleType
    );
}
