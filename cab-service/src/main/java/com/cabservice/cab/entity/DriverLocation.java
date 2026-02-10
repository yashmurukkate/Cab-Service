package com.cabservice.cab.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * DriverLocation Entity - Stores driver location history for tracking
 */
@Entity
@Table(name = "driver_locations", indexes = {
    @Index(name = "idx_driver_timestamp", columnList = "driver_id, timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double speed;

    @Column
    private Double heading;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
