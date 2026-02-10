package com.cabservice.user.service;

import com.cabservice.user.dto.*;
import com.cabservice.user.entity.RefreshToken;
import com.cabservice.user.entity.User;
import com.cabservice.user.entity.UserProfile;
import com.cabservice.user.exception.InvalidTokenException;
import com.cabservice.user.exception.ResourceNotFoundException;
import com.cabservice.user.exception.UserAlreadyExistsException;
import com.cabservice.user.kafka.UserEventProducer;
import com.cabservice.user.mapper.UserMapper;
import com.cabservice.user.repository.RefreshTokenRepository;
import com.cabservice.user.repository.UserProfileRepository;
import com.cabservice.user.repository.UserRepository;
import com.cabservice.user.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authentication Service - Handles user registration, login, and token management
 */
@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final UserEventProducer userEventProducer;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public AuthService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager,
                       UserMapper userMapper,
                       UserEventProducer userEventProducer) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        this.userEventProducer = userEventProducer;
    }

    /**
     * Register a new user
     */
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        // Check if phone already exists
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("Phone number already registered: " + request.getPhone());
        }

        // Create user entity
        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .status(User.UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phoneVerified(false)
                .emailVerificationToken(UUID.randomUUID().toString())
                .emailVerificationExpiry(LocalDateTime.now().plusHours(24))
                .build();

        user = userRepository.save(user);

        // Create user profile
        UserProfile profile = UserProfile.builder()
                .user(user)
                .totalRides(0)
                .build();
        userProfileRepository.save(profile);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = createRefreshToken(user);

        // Publish user registered event
        userEventProducer.sendUserRegisteredEvent(user);

        logger.info("User registered successfully: {}", user.getId());

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                userMapper.toUserDto(user)
        );
    }

    /**
     * Login user
     */
    public AuthResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            logger.warn("Login failed for email: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = createRefreshToken(user);

        logger.info("User logged in successfully: {}", user.getId());

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                userMapper.toUserDto(user)
        );
    }

    /**
     * Refresh access token
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (storedToken.isRevoked() || storedToken.isExpired()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        User user = storedToken.getUser();

        // Revoke old refresh token
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        // Generate new tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = createRefreshToken(user);

        logger.info("Token refreshed for user: {}", user.getId());

        return AuthResponse.of(
                accessToken,
                newRefreshToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                userMapper.toUserDto(user)
        );
    }

    /**
     * Logout user - revoke all refresh tokens
     */
    public void logout(Long userId) {
        logger.info("Logging out user: {}", userId);
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    /**
     * Verify email with token
     */
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (user.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiry(null);

        // Activate user if phone is also verified or verification not required
        if (user.isPhoneVerified()) {
            user.setStatus(User.UserStatus.ACTIVE);
        }

        userRepository.save(user);

        logger.info("Email verified for user: {}", user.getId());
    }

    /**
     * Send phone OTP
     */
    public void sendPhoneOtp(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Generate 6-digit OTP
        String otp = String.format("%06d", (int) (Math.random() * 1000000));
        user.setPhoneOtp(otp);
        user.setPhoneOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        // TODO: Send OTP via SMS service (notification-service)
        logger.info("Phone OTP generated for user: {} - OTP: {}", user.getId(), otp);
    }

    /**
     * Verify phone OTP
     */
    public void verifyPhone(Long userId, String otp) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getPhoneOtp() == null || !user.getPhoneOtp().equals(otp)) {
            throw new InvalidTokenException("Invalid OTP");
        }

        if (user.getPhoneOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("OTP has expired");
        }

        user.setPhoneVerified(true);
        user.setPhoneOtp(null);
        user.setPhoneOtpExpiry(null);

        // Activate user if email is also verified
        if (user.isEmailVerified()) {
            user.setStatus(User.UserStatus.ACTIVE);
        }

        userRepository.save(user);

        logger.info("Phone verified for user: {}", user.getId());
    }

    /**
     * Request password reset
     */
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // TODO: Send password reset email via notification-service
        logger.info("Password reset token generated for user: {}", user.getId());
    }

    /**
     * Reset password with token
     */
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);

        // Revoke all refresh tokens for security
        refreshTokenRepository.revokeAllByUserId(user.getId());

        logger.info("Password reset for user: {}", user.getId());
    }

    /**
     * Create and store refresh token
     */
    private String createRefreshToken(User user) {
        String token = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }
}
