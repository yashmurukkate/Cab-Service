package com.cabservice.cab.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Location Update Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationUpdateRequest {

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Double speed;

    private Double heading;
}
