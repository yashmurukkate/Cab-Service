package com.cabservice.billing.service;

import com.cabservice.billing.dto.*;
import com.cabservice.billing.entity.Invoice;
import com.cabservice.billing.entity.PromoCode;
import com.cabservice.billing.exception.ResourceNotFoundException;
import com.cabservice.billing.repository.InvoiceRepository;
import com.cabservice.billing.repository.PromoCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Billing Service - Handles fare calculation and invoice generation
 */
@Service
@Transactional
public class BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingService.class);

    private final InvoiceRepository invoiceRepository;
    private final PromoCodeRepository promoCodeRepository;

    // Fare configuration (can be moved to config file)
    private static final Map<String, BigDecimal> BASE_FARES = Map.of(
            "MINI", BigDecimal.valueOf(40),
            "SEDAN", BigDecimal.valueOf(60),
            "SUV", BigDecimal.valueOf(80),
            "PREMIUM", BigDecimal.valueOf(120)
    );

    private static final Map<String, BigDecimal> PER_KM_RATES = Map.of(
            "MINI", BigDecimal.valueOf(10),
            "SEDAN", BigDecimal.valueOf(14),
            "SUV", BigDecimal.valueOf(18),
            "PREMIUM", BigDecimal.valueOf(25)
    );

    private static final Map<String, BigDecimal> PER_MINUTE_RATES = Map.of(
            "MINI", BigDecimal.valueOf(1),
            "SEDAN", BigDecimal.valueOf(1.5),
            "SUV", BigDecimal.valueOf(2),
            "PREMIUM", BigDecimal.valueOf(3)
    );

    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.05); // 5% tax

    public BillingService(InvoiceRepository invoiceRepository, 
                          PromoCodeRepository promoCodeRepository) {
        this.invoiceRepository = invoiceRepository;
        this.promoCodeRepository = promoCodeRepository;
    }

    /**
     * Calculate estimated fare for a ride
     */
    public FareEstimateResponse calculateFare(FareCalculationRequest request) {
        logger.info("Calculating fare for vehicle type: {}", request.getVehicleType());

        // Calculate distance using Haversine formula
        BigDecimal distanceKm = calculateDistance(
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDropoffLatitude(), request.getDropoffLongitude()
        );

        // Estimate duration (average speed of 30 km/h in city)
        int durationMinutes = distanceKm.multiply(BigDecimal.valueOf(2)).intValue();

        String vehicleType = request.getVehicleType().toUpperCase();
        
        // Get rates
        BigDecimal baseFare = BASE_FARES.getOrDefault(vehicleType, BigDecimal.valueOf(50));
        BigDecimal perKmRate = PER_KM_RATES.getOrDefault(vehicleType, BigDecimal.valueOf(12));
        BigDecimal perMinuteRate = PER_MINUTE_RATES.getOrDefault(vehicleType, BigDecimal.valueOf(1.5));

        // Calculate charges
        BigDecimal distanceCharge = distanceKm.multiply(perKmRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal timeCharge = BigDecimal.valueOf(durationMinutes).multiply(perMinuteRate).setScale(2, RoundingMode.HALF_UP);

        // Calculate surge (mock - based on time of day)
        BigDecimal surgeMultiplier = calculateSurgeMultiplier();
        BigDecimal surgeCharge = BigDecimal.ZERO;
        if (surgeMultiplier.compareTo(BigDecimal.ONE) > 0) {
            surgeCharge = (baseFare.add(distanceCharge).add(timeCharge))
                    .multiply(surgeMultiplier.subtract(BigDecimal.ONE))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Calculate discount
        BigDecimal discount = BigDecimal.ZERO;
        if (request.getPromoCode() != null && !request.getPromoCode().isEmpty()) {
            discount = calculateDiscount(request.getPromoCode(), baseFare.add(distanceCharge).add(timeCharge).add(surgeCharge));
        }

        // Calculate subtotal and tax
        BigDecimal subtotal = baseFare.add(distanceCharge).add(timeCharge).add(surgeCharge).subtract(discount);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        return FareEstimateResponse.builder()
                .baseFare(baseFare)
                .distanceCharge(distanceCharge)
                .timeCharge(timeCharge)
                .surgeMultiplier(surgeMultiplier)
                .surgeCharge(surgeCharge)
                .discount(discount)
                .tax(tax)
                .estimatedFare(subtotal)
                .totalAmount(totalAmount)
                .estimatedDistanceKm(distanceKm)
                .estimatedDurationMinutes(durationMinutes)
                .build();
    }

    /**
     * Generate invoice for a completed ride
     */
    public InvoiceDto generateInvoice(Long rideId, Long customerId, Long driverId,
                                      BigDecimal distanceKm, Integer durationMinutes,
                                      String vehicleType, String promoCode) {
        logger.info("Generating invoice for ride: {}", rideId);

        // Check if invoice already exists
        if (invoiceRepository.findByRideId(rideId).isPresent()) {
            throw new IllegalStateException("Invoice already exists for ride: " + rideId);
        }

        // Calculate fare
        FareCalculationRequest fareRequest = FareCalculationRequest.builder()
                .pickupLatitude(0.0)
                .pickupLongitude(0.0)
                .dropoffLatitude(0.0)
                .dropoffLongitude(0.0)
                .vehicleType(vehicleType)
                .promoCode(promoCode)
                .build();

        // Use actual distance for calculation
        String vType = vehicleType.toUpperCase();
        BigDecimal baseFare = BASE_FARES.getOrDefault(vType, BigDecimal.valueOf(50));
        BigDecimal perKmRate = PER_KM_RATES.getOrDefault(vType, BigDecimal.valueOf(12));
        BigDecimal perMinuteRate = PER_MINUTE_RATES.getOrDefault(vType, BigDecimal.valueOf(1.5));

        BigDecimal distanceCharge = distanceKm.multiply(perKmRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal timeCharge = BigDecimal.valueOf(durationMinutes).multiply(perMinuteRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal surgeMultiplier = BigDecimal.ONE;
        BigDecimal surgeCharge = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;

        if (promoCode != null && !promoCode.isEmpty()) {
            discount = calculateDiscount(promoCode, baseFare.add(distanceCharge).add(timeCharge));
        }

        BigDecimal subtotal = baseFare.add(distanceCharge).add(timeCharge).subtract(discount);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(tax);

        // Generate invoice number
        String invoiceNumber = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .rideId(rideId)
                .customerId(customerId)
                .driverId(driverId)
                .baseFare(baseFare)
                .distanceCharge(distanceCharge)
                .timeCharge(timeCharge)
                .surgeMultiplier(surgeMultiplier)
                .surgeCharge(surgeCharge)
                .discount(discount)
                .tax(tax)
                .totalAmount(totalAmount)
                .distanceKm(distanceKm)
                .durationMinutes(durationMinutes)
                .vehicleType(vehicleType)
                .status(Invoice.InvoiceStatus.PENDING)
                .build();

        invoice = invoiceRepository.save(invoice);
        logger.info("Invoice generated: {}", invoice.getInvoiceNumber());

        return toInvoiceDto(invoice);
    }

    /**
     * Get invoice by ID
     */
    public InvoiceDto getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        return toInvoiceDto(invoice);
    }

    /**
     * Get invoice by ride ID
     */
    public InvoiceDto getInvoiceByRideId(Long rideId) {
        Invoice invoice = invoiceRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "rideId", rideId));
        return toInvoiceDto(invoice);
    }

    /**
     * Get customer invoices
     */
    public Page<InvoiceDto> getCustomerInvoices(Long customerId, int page, int size) {
        return invoiceRepository.findByCustomerId(customerId, 
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toInvoiceDto);
    }

    /**
     * Mark invoice as paid
     */
    public InvoiceDto markInvoicePaid(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));

        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoice = invoiceRepository.save(invoice);

        return toInvoiceDto(invoice);
    }

    // Helper methods
    private BigDecimal calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        double earthRadius = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;
        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSurgeMultiplier() {
        // Mock surge calculation based on current hour
        int hour = LocalDateTime.now().getHour();
        if ((hour >= 8 && hour <= 10) || (hour >= 17 && hour <= 20)) {
            return BigDecimal.valueOf(1.5);
        } else if (hour >= 22 || hour <= 5) {
            return BigDecimal.valueOf(1.2);
        }
        return BigDecimal.ONE;
    }

    private BigDecimal calculateDiscount(String promoCode, BigDecimal subtotal) {
        return promoCodeRepository.findByCode(promoCode.toUpperCase())
                .filter(pc -> pc.isActive() && 
                        (pc.getValidUntil() == null || pc.getValidUntil().isAfter(LocalDateTime.now())))
                .map(pc -> {
                    if (pc.getMinOrderValue() != null && subtotal.compareTo(pc.getMinOrderValue()) < 0) {
                        return BigDecimal.ZERO;
                    }
                    BigDecimal discount;
                    if (pc.getDiscountType() == PromoCode.DiscountType.PERCENTAGE) {
                        discount = subtotal.multiply(pc.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    } else {
                        discount = pc.getDiscountValue();
                    }
                    if (pc.getMaxDiscount() != null && discount.compareTo(pc.getMaxDiscount()) > 0) {
                        discount = pc.getMaxDiscount();
                    }
                    return discount;
                })
                .orElse(BigDecimal.ZERO);
    }

    private InvoiceDto toInvoiceDto(Invoice invoice) {
        return InvoiceDto.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .rideId(invoice.getRideId())
                .customerId(invoice.getCustomerId())
                .driverId(invoice.getDriverId())
                .baseFare(invoice.getBaseFare())
                .distanceCharge(invoice.getDistanceCharge())
                .timeCharge(invoice.getTimeCharge())
                .surgeMultiplier(invoice.getSurgeMultiplier())
                .surgeCharge(invoice.getSurgeCharge())
                .discount(invoice.getDiscount())
                .tax(invoice.getTax())
                .totalAmount(invoice.getTotalAmount())
                .distanceKm(invoice.getDistanceKm())
                .durationMinutes(invoice.getDurationMinutes())
                .vehicleType(invoice.getVehicleType())
                .status(invoice.getStatus())
                .createdAt(invoice.getCreatedAt())
                .paidAt(invoice.getPaidAt())
                .build();
    }
}
