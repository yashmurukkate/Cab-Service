package com.cabservice.billing.repository;

import com.cabservice.billing.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByInvoiceId(Long invoiceId);
    Page<Payment> findByCustomerId(Long customerId, Pageable pageable);
}
