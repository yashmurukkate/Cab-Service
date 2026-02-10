package com.cabservice.ride.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Feign Client for Cab Service
 */
@FeignClient(name = "CAB-SERVICE", path = "/api/cabs")
public interface CabServiceClient {

    @GetMapping("/drivers/{id}")
    Map<String, Object> getDriverById(@PathVariable Long id);

    @GetMapping("/nearby")
    Object[] findNearbyCabs(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radius,
            @RequestParam(required = false) String vehicleType
    );

    @PatchMapping("/drivers/{id}/status")
    Map<String, Object> updateDriverStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    );

    @PostMapping("/drivers/{id}/rating")
    Map<String, String> updateDriverRating(
            @PathVariable Long id,
            @RequestParam BigDecimal rating
    );
}
