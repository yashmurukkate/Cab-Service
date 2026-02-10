package com.cabservice.cab.dto;

import com.cabservice.cab.entity.Vehicle.VehicleType;
import lombok.*;

/**
 * Nearby Cabs Search Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyCabsRequest {

    private Double latitude;
    private Double longitude;
    private Double radiusKm;
    private VehicleType vehicleType;
}
