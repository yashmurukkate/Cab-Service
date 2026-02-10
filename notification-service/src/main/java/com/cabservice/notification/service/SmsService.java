package com.cabservice.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * SMS Service - Mock SMS notifications
 */
@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    /**
     * Send SMS (mock implementation)
     */
    public void sendSms(String phoneNumber, String message) {
        // Mock SMS sending - In production, integrate with Twilio, AWS SNS, etc.
        logger.info("SMS sent to {}: {}", phoneNumber, message);
    }

    public void sendOtp(String phoneNumber, String otp) {
        String message = String.format("Your CabService verification code is: %s. Valid for 10 minutes.", otp);
        sendSms(phoneNumber, message);
    }

    public void sendRideOtp(String phoneNumber, String otp, String driverName) {
        String message = String.format("Your ride OTP is: %s. Share with driver %s to start your trip.", otp, driverName);
        sendSms(phoneNumber, message);
    }

    public void sendDriverAssignedSms(String phoneNumber, String driverName, String vehicleNumber) {
        String message = String.format("Driver %s with vehicle %s is on the way!", driverName, vehicleNumber);
        sendSms(phoneNumber, message);
    }

    public void sendRideCompletedSms(String phoneNumber, String fare) {
        String message = String.format("Your ride is complete. Total fare: %s. Thank you for riding with CabService!", fare);
        sendSms(phoneNumber, message);
    }
}
