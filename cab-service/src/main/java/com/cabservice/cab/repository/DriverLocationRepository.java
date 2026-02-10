package com.cabservice.cab.repository;

import com.cabservice.cab.entity.DriverLocation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DriverLocation Repository
 */
@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {

    /**
     * Get last N locations for a driver
     */
    List<DriverLocation> findByDriverIdOrderByTimestampDesc(Long driverId, Pageable pageable);

    /**
     * Delete old location history (keep last 100 entries per driver)
     */
    @Modifying
    @Query(value = """
        DELETE FROM driver_locations 
        WHERE driver_id = :driverId 
        AND id NOT IN (
            SELECT id FROM (
                SELECT id FROM driver_locations 
                WHERE driver_id = :driverId 
                ORDER BY timestamp DESC 
                LIMIT 100
            ) AS recent
        )
        """, nativeQuery = true)
    void deleteOldLocations(Long driverId);

    /**
     * Delete locations older than specified time
     */
    @Modifying
    @Query("DELETE FROM DriverLocation dl WHERE dl.timestamp < :cutoff")
    void deleteLocationsOlderThan(LocalDateTime cutoff);
}
