package com.cabservice.user.mapper;

import com.cabservice.user.dto.PaymentMethodDto;
import com.cabservice.user.dto.UserDto;
import com.cabservice.user.entity.PaymentMethod;
import com.cabservice.user.entity.User;
import com.cabservice.user.entity.UserProfile;
import org.springframework.stereotype.Component;

/**
 * Entity to DTO Mapper
 */
@Component
public class UserMapper {

    /**
     * Convert User entity to UserDto
     */
    public UserDto toUserDto(User user) {
        if (user == null) return null;

        UserDto.UserDtoBuilder builder = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .createdAt(user.getCreatedAt());

        // Add profile info if exists
        UserProfile profile = user.getProfile();
        if (profile != null) {
            builder.address(profile.getAddress())
                    .city(profile.getCity())
                    .state(profile.getState())
                    .zipCode(profile.getZipCode())
                    .profileImageUrl(profile.getProfileImageUrl())
                    .rating(profile.getRating())
                    .totalRides(profile.getTotalRides());
        }

        return builder.build();
    }

    /**
     * Convert PaymentMethod entity to PaymentMethodDto
     */
    public PaymentMethodDto toPaymentMethodDto(PaymentMethod paymentMethod) {
        if (paymentMethod == null) return null;

        String maskedCardNumber = null;
        if (paymentMethod.getCardNumber() != null && paymentMethod.getCardNumber().length() >= 4) {
            maskedCardNumber = "**** **** **** " + 
                    paymentMethod.getCardNumber().substring(paymentMethod.getCardNumber().length() - 4);
        }

        return PaymentMethodDto.builder()
                .id(paymentMethod.getId())
                .type(paymentMethod.getType())
                .maskedCardNumber(maskedCardNumber)
                .cardHolderName(paymentMethod.getCardHolderName())
                .expiryDate(paymentMethod.getExpiryDate())
                .upiId(paymentMethod.getUpiId())
                .walletProvider(paymentMethod.getWalletProvider())
                .isDefault(paymentMethod.isDefault())
                .createdAt(paymentMethod.getCreatedAt())
                .build();
    }
}
