package com.cabservice.billing.dto;

import com.cabservice.billing.entity.Invoice.InvoiceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Invoice Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDto {

    private Long id;
    private String invoiceNumber;
    private Long rideId;
    private Long customerId;
    private Long driverId;
    private BigDecimal baseFare;
    private BigDecimal distanceCharge;
    private BigDecimal timeCharge;
    private BigDecimal surgeMultiplier;
    private BigDecimal surgeCharge;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private BigDecimal distanceKm;
    private Integer durationMinutes;
    private String vehicleType;
    private InvoiceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
