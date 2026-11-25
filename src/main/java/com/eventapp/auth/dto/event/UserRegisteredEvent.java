package com.eventapp.auth.dto.event;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID messageId,
        UUID userId,
        String phoneNumber,
        boolean phoneVerified,
        Instant createdAt
) {
}