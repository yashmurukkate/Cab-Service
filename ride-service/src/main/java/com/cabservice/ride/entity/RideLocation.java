package com.cabservice.ride.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * RideLocation Entity - Tracks ride progress locations
 */
@Entity
@Table(name = "ride_locations", indexes = {
    @Index(name = "idx_ride_timestamp", columnList = "ride_id, timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ride_id", nullable = false)
    private Long rideId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Double speed;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
