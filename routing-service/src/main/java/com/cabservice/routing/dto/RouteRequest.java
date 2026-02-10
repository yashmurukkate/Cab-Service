package com.cabservice.routing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Route Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteRequest {

    @NotNull
    private Double startLatitude;

    @NotNull
    private Double startLongitude;

    @NotNull
    private Double endLatitude;

    @NotNull
    private Double endLongitude;

    private String vehicleType;
}
