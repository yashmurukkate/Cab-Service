package com.cabservice.ride.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign Client for Routing Service
 */
@FeignClient(name = "ROUTING-SERVICE", path = "/api/routing")
public interface RoutingServiceClient {

    @PostMapping("/calculate-distance")
    Map<String, Object> calculateDistance(@RequestBody Map<String, Object> request);

    @PostMapping("/estimate-duration")
    Map<String, Object> estimateDuration(@RequestBody Map<String, Object> request);
}
