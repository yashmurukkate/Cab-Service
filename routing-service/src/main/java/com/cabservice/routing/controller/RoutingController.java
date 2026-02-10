package com.cabservice.routing.controller;

import com.cabservice.routing.dto.*;
import com.cabservice.routing.service.RoutingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Routing Controller
 */
@RestController
@RequestMapping("/api/routing")
@Tag(name = "Routing", description = "Distance calculation, ETA, and route APIs")
public class RoutingController {

    private final RoutingService routingService;

    public RoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @PostMapping("/calculate-distance")
    @Operation(summary = "Calculate distance between two points")
    public ResponseEntity<DistanceResponse> calculateDistance(@Valid @RequestBody RouteRequest request) {
        DistanceResponse response = routingService.calculateDistance(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/estimate-eta")
    @Operation(summary = "Estimate arrival time")
    public ResponseEntity<EtaResponse> estimateEta(@Valid @RequestBody RouteRequest request) {
        EtaResponse response = routingService.estimateEta(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/route")
    @Operation(summary = "Get route with polyline")
    public ResponseEntity<RouteResponse> getRoute(@Valid @RequestBody RouteRequest request) {
        RouteResponse response = routingService.getRoute(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/distance")
    @Operation(summary = "Calculate distance (GET)")
    public ResponseEntity<DistanceResponse> getDistance(
            @RequestParam Double startLat,
            @RequestParam Double startLon,
            @RequestParam Double endLat,
            @RequestParam Double endLon) {
        RouteRequest request = RouteRequest.builder()
                .startLatitude(startLat)
                .startLongitude(startLon)
                .endLatitude(endLat)
                .endLongitude(endLon)
                .build();
        return ResponseEntity.ok(routingService.calculateDistance(request));
    }

    @GetMapping("/eta")
    @Operation(summary = "Estimate ETA (GET)")
    public ResponseEntity<EtaResponse> getEta(
            @RequestParam Double startLat,
            @RequestParam Double startLon,
            @RequestParam Double endLat,
            @RequestParam Double endLon,
            @RequestParam(required = false) String vehicleType) {
        RouteRequest request = RouteRequest.builder()
                .startLatitude(startLat)
                .startLongitude(startLon)
                .endLatitude(endLat)
                .endLongitude(endLon)
                .vehicleType(vehicleType)
                .build();
        return ResponseEntity.ok(routingService.estimateEta(request));
    }
}
