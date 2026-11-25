package com.eventapp.auth.service;

import com.eventapp.auth.dto.event.UserRegisteredEvent;
import com.eventapp.auth.entity.AuthUser;
import com.eventapp.auth.entity.OutboxEvent;
import com.eventapp.auth.exception.OutboxEventException;
import com.eventapp.auth.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OutboxService {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxService.class);

    private static final String USER_REGISTERED_TOPIC =
            "cenes.auth.user-registered.v1";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {

        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveUserRegisteredEvent(AuthUser authUser) {

        if (!authUser.isPhoneVerified()) {
            throw new IllegalStateException(
                    "Cannot register an unverified user"
            );
        }

        UUID messageId = UUID.randomUUID();

        UserRegisteredEvent event = new UserRegisteredEvent(
                messageId,
                authUser.getId(),
                authUser.getPhoneNumber(),
                authUser.isPhoneVerified(),
                authUser.getCreatedAt()
        );

        try {

            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = new OutboxEvent(
                    messageId,
                    authUser.getId(),
                    "USER",
                    "USER_REGISTERED",
                    USER_REGISTERED_TOPIC,
                    payload
            );

            outboxEventRepository.save(outboxEvent);

            log.debug(
                    "User registration event prepared. userId={}, messageId={}",
                    authUser.getId(),
                    messageId
            );

        } catch (JsonProcessingException ex) {

            throw new OutboxEventException(
                    "Failed to serialize user registration event",
                    ex
            );
        }
    }
}