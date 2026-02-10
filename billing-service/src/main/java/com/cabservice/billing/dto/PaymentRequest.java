package com.cabservice.billing.dto;

import com.cabservice.billing.entity.Payment.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Payment Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull
    private Long invoiceId;

    @NotNull
    private PaymentMethod paymentMethod;

    private String paymentMethodDetails;
}
