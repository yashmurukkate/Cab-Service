package com.cabservice.user.service;

import com.cabservice.user.dto.*;
import com.cabservice.user.entity.PaymentMethod;
import com.cabservice.user.entity.User;
import com.cabservice.user.entity.UserProfile;
import com.cabservice.user.exception.ResourceNotFoundException;
import com.cabservice.user.kafka.UserEventProducer;
import com.cabservice.user.mapper.UserMapper;
import com.cabservice.user.repository.PaymentMethodRepository;
import com.cabservice.user.repository.UserProfileRepository;
import com.cabservice.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User Service - Handles user profile and payment method management
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final UserMapper userMapper;
    private final UserEventProducer userEventProducer;

    public UserService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       PaymentMethodRepository paymentMethodRepository,
                       UserMapper userMapper,
                       UserEventProducer userEventProducer) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.userMapper = userMapper;
        this.userEventProducer = userEventProducer;
    }

    /**
     * Get user by ID
     */
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toUserDto(user);
    }

    /**
     * Get user profile
     */
    public UserDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return userMapper.toUserDto(user);
    }

    /**
     * Update user profile
     */
    public UserDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Update user basic info
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        userRepository.save(user);

        // Update or create profile
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(UserProfile.builder().user(user).build());

        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            profile.setCity(request.getCity());
        }
        if (request.getState() != null) {
            profile.setState(request.getState());
        }
        if (request.getZipCode() != null) {
            profile.setZipCode(request.getZipCode());
        }
        if (request.getProfileImageUrl() != null) {
            profile.setProfileImageUrl(request.getProfileImageUrl());
        }

        userProfileRepository.save(profile);

        // Publish user updated event
        userEventProducer.sendUserUpdatedEvent(user);

        logger.info("Profile updated for user: {}", userId);
        return userMapper.toUserDto(user);
    }

    /**
     * Get user rating
     */
    public BigDecimal getUserRating(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));
        return profile.getRating() != null ? profile.getRating() : BigDecimal.ZERO;
    }

    /**
     * Update user rating
     */
    public void updateUserRating(Long userId, BigDecimal newRating) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        BigDecimal currentRating = profile.getRating() != null ? profile.getRating() : BigDecimal.ZERO;
        int totalRides = profile.getTotalRides() != null ? profile.getTotalRides() : 0;

        // Calculate new average rating
        BigDecimal totalPoints = currentRating.multiply(BigDecimal.valueOf(totalRides)).add(newRating);
        profile.setTotalRides(totalRides + 1);
        profile.setRating(totalPoints.divide(BigDecimal.valueOf(profile.getTotalRides()), 2, java.math.RoundingMode.HALF_UP));

        userProfileRepository.save(profile);
        logger.info("Rating updated for user: {} - New rating: {}", userId, profile.getRating());
    }

    /**
     * Add payment method
     */
    public PaymentMethodDto addPaymentMethod(Long userId, PaymentMethodRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // If this is set as default, clear other defaults
        if (request.isDefault()) {
            paymentMethodRepository.clearDefaultForUser(userId);
        }

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .user(user)
                .type(request.getType())
                .cardNumber(request.getCardNumber())
                .cardHolderName(request.getCardHolderName())
                .expiryDate(request.getExpiryDate())
                .upiId(request.getUpiId())
                .walletProvider(request.getWalletProvider())
                .isDefault(request.isDefault())
                .build();

        paymentMethod = paymentMethodRepository.save(paymentMethod);

        logger.info("Payment method added for user: {}", userId);
        return userMapper.toPaymentMethodDto(paymentMethod);
    }

    /**
     * Get all payment methods for user
     */
    public List<PaymentMethodDto> getPaymentMethods(Long userId) {
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findByUserId(userId);
        return paymentMethods.stream()
                .map(userMapper::toPaymentMethodDto)
                .collect(Collectors.toList());
    }

    /**
     * Delete payment method
     */
    public void deletePaymentMethod(Long userId, Long paymentMethodId) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentMethod", "id", paymentMethodId));

        if (!paymentMethod.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("PaymentMethod", "id", paymentMethodId);
        }

        paymentMethodRepository.delete(paymentMethod);
        logger.info("Payment method deleted: {} for user: {}", paymentMethodId, userId);
    }
}
