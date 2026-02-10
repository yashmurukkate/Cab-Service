package com.cabservice.routing.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Distance Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistanceResponse {

    private BigDecimal distanceKm;
    private String distanceText;
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
}
