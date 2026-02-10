package com.cabservice.cab.repository;

import com.cabservice.cab.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Vehicle Repository
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByDriverId(Long driverId);

    boolean existsByVehicleNumber(String vehicleNumber);

    boolean existsByRegistrationNumber(String registrationNumber);
}
