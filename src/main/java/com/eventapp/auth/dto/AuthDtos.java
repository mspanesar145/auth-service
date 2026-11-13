package com.eventapp.auth.dto;

import jakarta.validation.constraints.*;

public class AuthDtos {
    public record RequestOtp(
            @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Use E.164 format, e.g. +919876543210") String phoneNumber) {
    }

    public record VerifyOtp(@NotBlank String phoneNumber, @NotBlank @Size(min = 6, max = 6) String otp,
                            @Size(max = 100) String deviceName) {
    }

    public record Refresh(@NotBlank String refreshToken) {
    }

    public record Logout(@NotBlank String refreshToken) {
    }

    public record TokenResponse(String userId, String accessToken, String refreshToken, String tokenType,
                                long expiresIn) {
    }

    public record Message(String message) {
    }
}
