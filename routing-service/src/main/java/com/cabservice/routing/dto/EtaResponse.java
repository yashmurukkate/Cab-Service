package com.cabservice.routing.dto;

import lombok.*;

/**
 * ETA Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtaResponse {

    private Integer durationMinutes;
    private String durationText;
    private String arrivalTime;
}
