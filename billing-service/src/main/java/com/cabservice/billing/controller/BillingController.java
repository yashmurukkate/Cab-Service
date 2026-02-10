package com.cabservice.billing.controller;

import com.cabservice.billing.dto.*;
import com.cabservice.billing.service.BillingService;
import com.cabservice.billing.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Billing Controller - Handles fare and payment endpoints
 */
@RestController
@RequestMapping("/api/billing")
@Tag(name = "Billing", description = "Fare calculation, invoicing, and payment APIs")
public class BillingController {

    private final BillingService billingService;
    private final PaymentService paymentService;

    public BillingController(BillingService billingService, PaymentService paymentService) {
        this.billingService = billingService;
        this.paymentService = paymentService;
    }

    @PostMapping("/calculate-fare")
    @Operation(summary = "Calculate estimated fare")
    public ResponseEntity<FareEstimateResponse> calculateFare(
            @Valid @RequestBody FareCalculationRequest request) {
        FareEstimateResponse response = billingService.calculateFare(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invoices")
    @Operation(summary = "Generate invoice for a ride")
    public ResponseEntity<InvoiceDto> generateInvoice(@RequestBody Map<String, Object> request) {
        Long rideId = Long.valueOf(request.get("rideId").toString());
        Long customerId = Long.valueOf(request.get("customerId").toString());
        Long driverId = request.get("driverId") != null ? Long.valueOf(request.get("driverId").toString()) : null;
        BigDecimal distanceKm = new BigDecimal(request.getOrDefault("distanceKm", "5").toString());
        Integer durationMinutes = Integer.valueOf(request.getOrDefault("durationMinutes", "15").toString());
        String vehicleType = request.getOrDefault("vehicleType", "SEDAN").toString();
        String promoCode = request.get("promoCode") != null ? request.get("promoCode").toString() : null;

        InvoiceDto invoice = billingService.generateInvoice(rideId, customerId, driverId, 
                distanceKm, durationMinutes, vehicleType, promoCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    @GetMapping("/invoices/{id}")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long id) {
        InvoiceDto invoice = billingService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/invoices/ride/{rideId}")
    @Operation(summary = "Get invoice by ride ID")
    public ResponseEntity<InvoiceDto> getInvoiceByRideId(@PathVariable Long rideId) {
        InvoiceDto invoice = billingService.getInvoiceByRideId(rideId);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/invoices/customer/{customerId}")
    @Operation(summary = "Get customer invoices")
    public ResponseEntity<Page<InvoiceDto>> getCustomerInvoices(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<InvoiceDto> invoices = billingService.getCustomerInvoices(customerId, page, size);
        return ResponseEntity.ok(invoices);
    }

    @PostMapping("/payments")
    @Operation(summary = "Process payment")
    public ResponseEntity<Map<String, Object>> processPayment(
            @RequestHeader("X-User-Id") Long customerId,
            @Valid @RequestBody PaymentRequest request) {
        Map<String, Object> result = paymentService.processPayment(customerId, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/payments/{transactionId}/refund")
    @Operation(summary = "Process refund")
    public ResponseEntity<Map<String, Object>> processRefund(@PathVariable String transactionId) {
        Map<String, Object> result = paymentService.processRefund(transactionId);
        return ResponseEntity.ok(result);
    }
}
