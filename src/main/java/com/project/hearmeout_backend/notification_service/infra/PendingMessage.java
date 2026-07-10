package com.project.hearmeout_backend.notification_service.infra;

import java.time.Instant;
import lombok.Builder;

@Builder
public record PendingMessage(
    String messageId,
    String exchange,
    String routingKey,
    Object payload,
    int attemptCount,
    Instant firstAttemptAt,
    Instant lastAttemptAt) {}
