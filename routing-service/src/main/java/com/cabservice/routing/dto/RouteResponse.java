package com.cabservice.routing.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Route Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteResponse {

    private BigDecimal distanceKm;
    private Integer durationMinutes;
    private String durationText;
    private String distanceText;
    private List<Coordinate> polyline;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Coordinate {
        private Double latitude;
        private Double longitude;
    }
}
