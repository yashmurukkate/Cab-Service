package com.cabservice.billing.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Fare Calculation Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareEstimateResponse {

    private BigDecimal baseFare;
    private BigDecimal distanceCharge;
    private BigDecimal timeCharge;
    private BigDecimal surgeMultiplier;
    private BigDecimal surgeCharge;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal estimatedFare;
    private BigDecimal totalAmount;
    private BigDecimal estimatedDistanceKm;
    private Integer estimatedDurationMinutes;
}
