package com.cabservice.routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Routing Service Application
 * Handles distance calculation, ETA estimation, and route optimization.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
public class RoutingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutingServiceApplication.class, args);
    }
}
