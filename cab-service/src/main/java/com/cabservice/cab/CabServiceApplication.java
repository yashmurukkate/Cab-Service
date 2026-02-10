package com.cabservice.cab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Cab Service Application
 * Handles driver management, vehicle registration, and real-time location tracking.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class CabServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CabServiceApplication.class, args);
    }
}
