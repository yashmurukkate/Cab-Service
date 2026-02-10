package com.cabservice.billing.service;

import com.cabservice.billing.dto.PaymentRequest;
import com.cabservice.billing.entity.Invoice;
import com.cabservice.billing.entity.Payment;
import com.cabservice.billing.exception.ResourceNotFoundException;
import com.cabservice.billing.repository.InvoiceRepository;
import com.cabservice.billing.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Payment Service - Handles payment processing (mock implementation)
 */
@Service
@Transactional
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final BillingService billingService;

    public PaymentService(PaymentRepository paymentRepository,
                          InvoiceRepository invoiceRepository,
                          BillingService billingService) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.billingService = billingService;
    }

    /**
     * Process payment for an invoice
     */
    public Map<String, Object> processPayment(Long customerId, PaymentRequest request) {
        logger.info("Processing payment for invoice: {}", request.getInvoiceId());

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", request.getInvoiceId()));

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new IllegalStateException("Invoice already paid");
        }

        // Check if payment already exists
        if (paymentRepository.findByInvoiceId(invoice.getId()).isPresent()) {
            throw new IllegalStateException("Payment already initiated for this invoice");
        }

        // Create payment record
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        Payment payment = Payment.builder()
                .transactionId(transactionId)
                .invoiceId(invoice.getId())
                .customerId(customerId)
                .amount(invoice.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentMethodDetails(request.getPaymentMethodDetails())
                .status(Payment.PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        // Mock payment processing
        boolean paymentSuccess = processWithPaymentGateway(payment);

        if (paymentSuccess) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setProcessedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Mark invoice as paid
            billingService.markInvoicePaid(invoice.getId());

            logger.info("Payment successful: {}", transactionId);
            return Map.of(
                    "success", true,
                    "transactionId", transactionId,
                    "message", "Payment processed successfully"
            );
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason("Payment gateway declined");
            payment.setProcessedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            logger.warn("Payment failed: {}", transactionId);
            return Map.of(
                    "success", false,
                    "transactionId", transactionId,
                    "message", "Payment failed"
            );
        }
    }

    /**
     * Get payment by transaction ID
     */
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", transactionId));
    }

    /**
     * Process refund
     */
    public Map<String, Object> processRefund(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", transactionId));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only successful payments can be refunded");
        }

        // Mock refund processing
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setProcessedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update invoice status
        Invoice invoice = invoiceRepository.findById(payment.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", payment.getInvoiceId()));
        invoice.setStatus(Invoice.InvoiceStatus.REFUNDED);
        invoiceRepository.save(invoice);

        logger.info("Refund processed for transaction: {}", transactionId);
        return Map.of(
                "success", true,
                "transactionId", transactionId,
                "message", "Refund processed successfully"
        );
    }

    /**
     * Mock payment gateway processing
     */
    private boolean processWithPaymentGateway(Payment payment) {
        // Simulate payment processing with 95% success rate
        if (payment.getPaymentMethod() == Payment.PaymentMethod.CASH) {
            return true; // Cash payments always succeed
        }
        return Math.random() < 0.95;
    }
}
