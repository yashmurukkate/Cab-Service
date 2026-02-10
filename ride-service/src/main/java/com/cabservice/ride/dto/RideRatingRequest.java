package com.cabservice.ride.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Ride Rating Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRatingRequest {

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5")
    private BigDecimal rating;

    private String feedback;
}
