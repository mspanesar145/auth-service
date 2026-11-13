package com.eventapp.auth.controller;

import com.eventapp.auth.dto.AuthDtos.*;
import com.eventapp.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/otp/request")
    public ResponseEntity<Message> request(@Valid @RequestBody RequestOtp req) {
        auth.requestOtp(req.phoneNumber());
        return ResponseEntity.accepted().body(new Message("OTP request accepted"));
    }

    @PostMapping("/otp/verify")
    public TokenResponse verify(@Valid @RequestBody VerifyOtp req) {
        return auth.verifyOtp(req);
    }

    @PostMapping("/token/refresh")
    public TokenResponse refresh(@Valid @RequestBody Refresh req) {
        return auth.refresh(req.refreshToken());
    }

    @PostMapping("/logout")
    public Message logout(@Valid @RequestBody Logout req) {
        auth.logout(req.refreshToken());
        return new Message("Logged out");
    }

    @PostMapping("/logout-all")
    public Message logoutAll(@AuthenticationPrincipal Jwt jwt) {
        auth.logoutAll(UUID.fromString(jwt.getSubject()));
        return new Message("All sessions revoked");
    }

}
