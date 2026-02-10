package com.cabservice.user.dto;

import com.cabservice.user.entity.User.UserRole;
import com.cabservice.user.entity.User.UserStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * User DTO - Response representation of User
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private UserRole role;
    private UserStatus status;
    private boolean emailVerified;
    private boolean phoneVerified;
    private LocalDateTime createdAt;
    
    // Profile information
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String profileImageUrl;
    private BigDecimal rating;
    private Integer totalRides;
}
