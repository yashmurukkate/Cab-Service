package com.cabservice.ride.repository;

import com.cabservice.ride.entity.RideLocation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * RideLocation Repository
 */
@Repository
public interface RideLocationRepository extends JpaRepository<RideLocation, Long> {

    List<RideLocation> findByRideIdOrderByTimestampDesc(Long rideId, Pageable pageable);

    List<RideLocation> findByRideIdOrderByTimestampAsc(Long rideId);
}
