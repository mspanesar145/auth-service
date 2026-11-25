package com.eventapp.auth.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, length = 200)
    private String topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "next_retry_at", nullable = false)
    private Instant nextRetryAt = Instant.now();

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    public enum Status {
        PENDING,
        PUBLISHED,
        FAILED
    }

    protected OutboxEvent() {
    }

    public OutboxEvent(
            UUID id,
            UUID aggregateId,
            String aggregateType,
            String eventType,
            String topic,
            String payload) {

        this.id = id;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.topic = topic;
        this.payload = payload;
    }

    public void markPublished() {
        this.status = Status.PUBLISHED;
        this.publishedAt = Instant.now();
        this.lastError = null;
    }

    public void markRetry(
            String sanitizedError,
            int maxRetries,
            long retryDelaySeconds) {

        this.retryCount++;
        this.lastError = sanitizedError;

        if (this.retryCount >= maxRetries) {
            this.status = Status.FAILED;
        } else {
            this.status = Status.PENDING;
            this.nextRetryAt = Instant.now()
                    .plusSeconds(retryDelaySeconds);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getEventType() {
        return eventType;
    }

    public String getTopic() {
        return topic;
    }

    public String getPayload() {
        return payload;
    }

    public Status getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getNextRetryAt() {
        return nextRetryAt;
    }

    public String getLastError() {
        return lastError;
    }
}