package com.eventapp.auth.service;

import com.eventapp.auth.dto.AuthDtos.*;
import com.eventapp.auth.entity.*;
import com.eventapp.auth.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Service
public class AuthService {
    private final AuthUserRepository users;
    private final OtpChallengeRepository otps;
    private final RefreshSessionRepository sessions;
    private final OtpDeliveryService delivery;
    private final TokenService tokens;
    private final SecureRandom random = new SecureRandom();

    @Value("${auth.otp.expiry-seconds:300}")
    long otpExpiry;
    @Value("${auth.otp.max-attempts:5}")
    int maxAttempts;
    @Value("${auth.otp.resend-cooldown-seconds:60}")
    long cooldown;
    @Value("${auth.refresh-token-days:30}")
    long refreshDays;
    @Value("${testMode:false}")
    boolean isTestMode;


 public AuthService(AuthUserRepository u, OtpChallengeRepository o, RefreshSessionRepository s, OtpDeliveryService d, TokenService t) {
        users = u;
        otps = o;
        sessions = s;
        delivery = d;
        tokens = t;
    }

    @Transactional
    public void requestOtp(String phone) {
        if (isTestMode) {
         return;
        }
        Instant now = Instant.now();
        otps.findTopByPhoneNumberAndConsumedFalseOrderByCreatedAtDesc(phone).ifPresent(old -> {
            if (old.getCreatedAt().plusSeconds(cooldown).isAfter(now))
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Please wait before requesting another OTP");
            old.consume();
        });
        String otp = String.format("%06d", random.nextInt(1_000_000));
        otps.save(new OtpChallenge(phone, hash(otp), now.plusSeconds(otpExpiry)));
        delivery.send(phone, otp);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public TokenResponse verifyOtp(VerifyOtp req) {
     if (!isTestMode) {
       OtpChallenge challenge = otps.findTopByPhoneNumberAndConsumedFalseOrderByCreatedAtDesc(req.phoneNumber()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP not found or already used"));
       if (challenge.getExpiresAt().isBefore(Instant.now())) {
        challenge.consume();
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
       }
       if (challenge.getAttempts() >= maxAttempts) {
        challenge.consume();
        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Maximum OTP attempts exceeded");
       }
       if (!MessageDigest.isEqual(challenge.getCodeHash().getBytes(StandardCharsets.UTF_8), hash(req.otp()).getBytes(StandardCharsets.UTF_8))) {
        challenge.incrementAttempts();
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
       }
       challenge.consume();
     }

     AuthUser user = users.findByPhoneNumber(req.phoneNumber()).orElseGet(() -> users.save(new AuthUser(req.phoneNumber())));
     if (user.getStatus() == AuthUser.Status.DISABLED || user.getStatus() == AuthUser.Status.DELETION_PENDING || user.getStatus() == AuthUser.Status.DELETED)
         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
     user.setPhoneVerified(true);
     user.setStatus(AuthUser.Status.ACTIVE);
     return issue(user, req.deviceName());
    }

    @Transactional
    public TokenResponse refresh(String refresh) {
        String hash = hash(refresh);
        RefreshSession old = sessions.findByTokenHash(hash).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (old.getRevokedAt() != null || old.getExpiresAt().isBefore(Instant.now()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
        AuthUser user = users.findById(old.getUserId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (user.getStatus() != AuthUser.Status.ACTIVE)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
        old.revoke();
        return issue(user, old.getDeviceName());
    }

    @Transactional
    public void logout(String refresh) {
        sessions.findByTokenHash(hash(refresh)).ifPresent(RefreshSession::revoke);
    }

    @Transactional
    public void logoutAll(UUID userId) {
        sessions.findByUserIdAndRevokedAtIsNull(userId).forEach(RefreshSession::revoke);
    }

    private TokenResponse issue(AuthUser user, String device) {
        String refresh = randomToken();
        sessions.save(new RefreshSession(user.getId(), hash(refresh), device, Instant.now().plusSeconds(refreshDays * 86400)));
        return new TokenResponse(user.getId().toString(), tokens.createAccessToken(user.getId()), refresh, "Bearer", tokens.getAccessSeconds());
    }

    private String randomToken() {
        byte[] b = new byte[48];
        random.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
