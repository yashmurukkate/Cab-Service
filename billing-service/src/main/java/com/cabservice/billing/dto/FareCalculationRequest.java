package com.cabservice.billing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Fare Calculation Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareCalculationRequest {

    @NotNull
    private Double pickupLatitude;

    @NotNull
    private Double pickupLongitude;

    @NotNull
    private Double dropoffLatitude;

    @NotNull
    private Double dropoffLongitude;

    @NotNull
    private String vehicleType;

    private String promoCode;
}
