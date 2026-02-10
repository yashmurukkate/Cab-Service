package com.cabservice.user.controller;

import com.cabservice.user.dto.*;
import com.cabservice.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * User Controller - Handles user profile and payment method endpoints
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User profile and payment method APIs")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserDto> getProfile(@RequestHeader("X-User-Id") Long userId) {
        UserDto user = userService.getUserProfile(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    public ResponseEntity<UserDto> updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserDto user = userService.updateProfile(userId, request);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/rating")
    @Operation(summary = "Get user rating")
    public ResponseEntity<Map<String, BigDecimal>> getUserRating(@PathVariable Long id) {
        BigDecimal rating = userService.getUserRating(id);
        return ResponseEntity.ok(Map.of("rating", rating));
    }

    @PostMapping("/payment-methods")
    @Operation(summary = "Add a payment method")
    public ResponseEntity<PaymentMethodDto> addPaymentMethod(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PaymentMethodRequest request) {
        PaymentMethodDto paymentMethod = userService.addPaymentMethod(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentMethod);
    }

    @GetMapping("/payment-methods")
    @Operation(summary = "Get all payment methods")
    public ResponseEntity<List<PaymentMethodDto>> getPaymentMethods(
            @RequestHeader("X-User-Id") Long userId) {
        List<PaymentMethodDto> paymentMethods = userService.getPaymentMethods(userId);
        return ResponseEntity.ok(paymentMethods);
    }

    @DeleteMapping("/payment-methods/{id}")
    @Operation(summary = "Delete a payment method")
    public ResponseEntity<Map<String, String>> deletePaymentMethod(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        userService.deletePaymentMethod(userId, id);
        return ResponseEntity.ok(Map.of("message", "Payment method deleted successfully"));
    }
}
