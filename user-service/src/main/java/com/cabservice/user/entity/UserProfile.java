package com.cabservice.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * UserProfile Entity - Extended user profile information
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String address;

    private String city;

    private String state;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "total_rides")
    private Integer totalRides;
}
