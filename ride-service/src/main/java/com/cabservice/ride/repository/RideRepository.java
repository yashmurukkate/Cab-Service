package com.cabservice.ride.repository;

import com.cabservice.ride.entity.Ride;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Ride Repository
 */
@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

    Page<Ride> findByCustomerId(Long customerId, Pageable pageable);

    Page<Ride> findByDriverId(Long driverId, Pageable pageable);

    List<Ride> findByStatus(Ride.RideStatus status);

    @Query("SELECT r FROM Ride r WHERE r.status IN ('REQUESTED', 'SEARCHING_DRIVER', 'ACCEPTED', 'DRIVER_ARRIVED', 'IN_PROGRESS')")
    List<Ride> findActiveRides();

    @Query("SELECT r FROM Ride r WHERE r.customerId = :customerId AND r.status IN ('REQUESTED', 'SEARCHING_DRIVER', 'ACCEPTED', 'DRIVER_ARRIVED', 'IN_PROGRESS')")
    Optional<Ride> findActiveRideByCustomerId(Long customerId);

    @Query("SELECT r FROM Ride r WHERE r.driverId = :driverId AND r.status IN ('ACCEPTED', 'DRIVER_ARRIVED', 'IN_PROGRESS')")
    Optional<Ride> findActiveRideByDriverId(Long driverId);
}
