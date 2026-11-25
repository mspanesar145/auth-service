package com.eventapp.auth.service;

import com.eventapp.auth.entity.OutboxEvent;
import com.eventapp.auth.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(
        name = "app.outbox.publisher.enabled",
        havingValue = "true"
)
public class OutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);

    private static final int MAX_RETRIES = 10;

    private final OutboxEventRepository repository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(
            OutboxEventRepository repository,
            KafkaTemplate<String, String> kafkaTemplate) {

        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;

        log.info("OutboxPublisher initialized successfully");
    }

    @Scheduled(
            fixedDelayString = "${app.outbox.publisher.delay-ms:1000}"
    )
    @Transactional
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                repository.findPendingEvents(10);

        for (OutboxEvent event : events) {

            try {

                kafkaTemplate.send(
                        event.getTopic(),
                        event.getAggregateId().toString(),
                        event.getPayload()
                ).get(15, TimeUnit.SECONDS);

                event.markPublished();

                log.info(
                        "Outbox event published. messageId={}, userId={}",
                        event.getId(),
                        event.getAggregateId()
                );

            } catch (InterruptedException ex) {

                Thread.currentThread().interrupt();

                throw new IllegalStateException(
                        "Outbox publisher interrupted", ex
                );

            } catch (Exception ex) {

                long retryDelay = Math.min(
                        300L,
                        5L * (1L << Math.min(
                                event.getRetryCount(), 6))
                );

                event.markRetry(
                        ex.getClass().getSimpleName(),
                        MAX_RETRIES,
                        retryDelay
                );

                log.warn(
                        "Outbox publishing failed. messageId={}, " +
                                "retryCount={}, status={}, errorType={}",
                        event.getId(),
                        event.getRetryCount(),
                        event.getStatus(),
                        ex.getClass().getSimpleName()
                );
            }
        }
    }
}