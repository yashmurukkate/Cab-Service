package com.cabservice.ride.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign Client for Billing Service
 */
@FeignClient(name = "BILLING-SERVICE", path = "/api/billing")
public interface BillingServiceClient {

    @PostMapping("/calculate-fare")
    Map<String, Object> calculateFare(@RequestBody Map<String, Object> request);

    @PostMapping("/invoices")
    Map<String, Object> generateInvoice(@RequestBody Map<String, Object> request);
}
