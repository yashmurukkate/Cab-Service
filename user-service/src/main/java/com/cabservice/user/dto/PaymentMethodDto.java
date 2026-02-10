package com.cabservice.user.dto;

import com.cabservice.user.entity.PaymentMethod.PaymentType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Payment Method Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodDto {

    private Long id;
    private PaymentType type;
    private String maskedCardNumber; // Only last 4 digits
    private String cardHolderName;
    private String expiryDate;
    private String upiId;
    private String walletProvider;
    private boolean isDefault;
    private LocalDateTime createdAt;
}
