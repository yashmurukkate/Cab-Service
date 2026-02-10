package com.cabservice.billing.repository;

import com.cabservice.billing.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByRideId(Long rideId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Page<Invoice> findByCustomerId(Long customerId, Pageable pageable);
    Page<Invoice> findByDriverId(Long driverId, Pageable pageable);
}
