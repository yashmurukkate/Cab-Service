package com.cabservice.cab.dto;

import com.cabservice.cab.entity.Driver.DriverStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Status Update Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private DriverStatus status;
}
