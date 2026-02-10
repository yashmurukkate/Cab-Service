package com.cabservice.routing.service;

import com.cabservice.routing.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Routing Service - Handles distance calculation, ETA, and routing
 */
@Service
public class RoutingService {

    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);
    private static final double EARTH_RADIUS_KM = 6371;

    // Average speeds by vehicle type (km/h) in city traffic
    private static final Map<String, Double> AVERAGE_SPEEDS = Map.of(
            "MINI", 25.0,
            "SEDAN", 28.0,
            "SUV", 26.0,
            "PREMIUM", 30.0
    );

    /**
     * Calculate distance between two points using Haversine formula
     */
    @Cacheable(value = "distances", key = "#request.startLatitude + '-' + #request.startLongitude + '-' + #request.endLatitude + '-' + #request.endLongitude")
    public DistanceResponse calculateDistance(RouteRequest request) {
        logger.info("Calculating distance from ({},{}) to ({},{})",
                request.getStartLatitude(), request.getStartLongitude(),
                request.getEndLatitude(), request.getEndLongitude());

        double distance = haversineDistance(
                request.getStartLatitude(), request.getStartLongitude(),
                request.getEndLatitude(), request.getEndLongitude()
        );

        BigDecimal distanceKm = BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
        String distanceText = formatDistance(distance);

        return DistanceResponse.builder()
                .distanceKm(distanceKm)
                .distanceText(distanceText)
                .startLatitude(request.getStartLatitude())
                .startLongitude(request.getStartLongitude())
                .endLatitude(request.getEndLatitude())
                .endLongitude(request.getEndLongitude())
                .build();
    }

    /**
     * Estimate ETA based on distance and traffic conditions
     */
    public EtaResponse estimateEta(RouteRequest request) {
        double distance = haversineDistance(
                request.getStartLatitude(), request.getStartLongitude(),
                request.getEndLatitude(), request.getEndLongitude()
        );

        String vehicleType = request.getVehicleType() != null ? request.getVehicleType().toUpperCase() : "SEDAN";
        double avgSpeed = AVERAGE_SPEEDS.getOrDefault(vehicleType, 28.0);

        // Apply traffic factor based on time of day
        double trafficFactor = getTrafficFactor();
        double adjustedSpeed = avgSpeed / trafficFactor;

        // Calculate duration in minutes
        double hours = distance / adjustedSpeed;
        int durationMinutes = Math.max(1, (int) Math.ceil(hours * 60));

        // Calculate arrival time
        LocalDateTime arrivalTime = LocalDateTime.now().plusMinutes(durationMinutes);
        String arrivalTimeStr = arrivalTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        return EtaResponse.builder()
                .durationMinutes(durationMinutes)
                .durationText(formatDuration(durationMinutes))
                .arrivalTime(arrivalTimeStr)
                .build();
    }

    /**
     * Get full route with polyline and duration
     */
    @Cacheable(value = "routes", key = "#request.startLatitude + '-' + #request.startLongitude + '-' + #request.endLatitude + '-' + #request.endLongitude")
    public RouteResponse getRoute(RouteRequest request) {
        double distance = haversineDistance(
                request.getStartLatitude(), request.getStartLongitude(),
                request.getEndLatitude(), request.getEndLongitude()
        );

        String vehicleType = request.getVehicleType() != null ? request.getVehicleType().toUpperCase() : "SEDAN";
        double avgSpeed = AVERAGE_SPEEDS.getOrDefault(vehicleType, 28.0);
        double trafficFactor = getTrafficFactor();
        double adjustedSpeed = avgSpeed / trafficFactor;
        int durationMinutes = Math.max(1, (int) Math.ceil((distance / adjustedSpeed) * 60));

        // Generate simplified polyline (straight line interpolation)
        List<RouteResponse.Coordinate> polyline = generatePolyline(
                request.getStartLatitude(), request.getStartLongitude(),
                request.getEndLatitude(), request.getEndLongitude(),
                10 // number of points
        );

        return RouteResponse.builder()
                .distanceKm(BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP))
                .durationMinutes(durationMinutes)
                .durationText(formatDuration(durationMinutes))
                .distanceText(formatDistance(distance))
                .polyline(polyline)
                .build();
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Apply road factor (1.3x to account for actual road distance vs straight line)
        return EARTH_RADIUS_KM * c * 1.3;
    }

    /**
     * Get traffic factor based on time of day
     */
    private double getTrafficFactor() {
        int hour = LocalDateTime.now().getHour();

        // Peak hours: 8-10am and 5-8pm
        if ((hour >= 8 && hour <= 10) || (hour >= 17 && hour <= 20)) {
            return 1.5; // 50% slower
        }
        // Night hours: 10pm-6am
        else if (hour >= 22 || hour <= 6) {
            return 0.8; // 20% faster
        }
        // Normal hours
        return 1.0;
    }

    /**
     * Generate polyline points between two coordinates
     */
    private List<RouteResponse.Coordinate> generatePolyline(
            double startLat, double startLon, double endLat, double endLon, int numPoints) {
        List<RouteResponse.Coordinate> points = new ArrayList<>();

        for (int i = 0; i <= numPoints; i++) {
            double fraction = (double) i / numPoints;
            double lat = startLat + (endLat - startLat) * fraction;
            double lon = startLon + (endLon - startLon) * fraction;

            // Add slight variation to simulate actual road
            if (i > 0 && i < numPoints) {
                lat += (Math.random() - 0.5) * 0.002;
                lon += (Math.random() - 0.5) * 0.002;
            }

            points.add(RouteResponse.Coordinate.builder()
                    .latitude(Math.round(lat * 100000.0) / 100000.0)
                    .longitude(Math.round(lon * 100000.0) / 100000.0)
                    .build());
        }

        return points;
    }

    private String formatDistance(double distanceKm) {
        if (distanceKm < 1) {
            return String.format("%d m", (int) (distanceKm * 1000));
        }
        return String.format("%.1f km", distanceKm);
    }

    private String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + " min";
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        return hours + " hr " + mins + " min";
    }
}
