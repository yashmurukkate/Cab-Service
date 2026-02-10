package com.cabservice.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Refresh Token Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
