package com.project.hearmeout_backend.notification_service.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.hearmeout_backend.notification_service.infra.PendingMessage;
import com.project.hearmeout_backend.notification_service.infra.PendingMessageStore;
import com.project.hearmeout_backend.notification_service.infra.PublisherDLQPublisher;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomRabbitTemplate {

  private static final int MAX_CONFIRM_RETRIES = 3;
  private static final int MAX_RETURN_RETRIES = 1;

  private final RabbitTemplate rabbitTemplate;
  private final PendingMessageStore pendingMessageStore;
  private final PublisherDLQPublisher dlqPublisher;
  private final TaskScheduler taskScheduler;

  public CustomRabbitTemplate(
      RabbitTemplate rabbitTemplate,
      PendingMessageStore pendingMessageStore,
      PublisherDLQPublisher dlqPublisher,
      TaskScheduler taskScheduler) {
    this.rabbitTemplate = rabbitTemplate;
    this.pendingMessageStore = pendingMessageStore;
    this.dlqPublisher = dlqPublisher;
    this.taskScheduler = taskScheduler;

    this.rabbitTemplate.setConfirmCallback(this::handleConfirm);
    this.rabbitTemplate.setReturnsCallback(this::handleReturn);
  }

  public void send(String exchange, String routingKey, Object payload)
      throws JsonProcessingException {
    send(exchange, routingKey, payload, 1, Instant.now());
  }

  private void send(
      String exchange, String routingKey, Object payload, int attempt, Instant firstAttemptAt)
      throws JsonProcessingException {
    String messageId = UUID.randomUUID().toString();
    CorrelationData correlationData = new CorrelationData(messageId);

    pendingMessageStore.put(
        PendingMessage.builder()
            .messageId(messageId)
            .exchange(exchange)
            .routingKey(routingKey)
            .payload(payload)
            .attemptCount(attempt)
            .firstAttemptAt(firstAttemptAt)
            .lastAttemptAt(Instant.now())
            .build());

    rabbitTemplate.convertAndSend(
        exchange,
        routingKey,
        payload,
        message -> {
          message.getMessageProperties().setMessageId(messageId);
          message.getMessageProperties().setHeader("x-attempt", attempt);
          message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
          return message;
        },
        correlationData);

    log.info(
        "[PUBLISH] exchange={} routingKey={} messageId={} attempt={}",
        exchange,
        routingKey,
        messageId,
        attempt);
  }

  private void handleConfirm(CorrelationData correlationData, boolean ack, String cause) {
    if (correlationData == null) {
      log.warn(
          "[CONFIRM] Received confirm with null correlationData — cannot map to pending message");
      return;
    }

    String messageId = correlationData.getId();
    Optional<PendingMessage> pending = pendingMessageStore.get(messageId);

    if (pending.isEmpty()) {
      log.warn("[CONFIRM] No pending message found for messageId={}", messageId);
      return;
    }

    if (ack) {
      log.info("[CONFIRM-ACK] messageId={}", messageId);
      pendingMessageStore.remove(messageId);
      return;
    }

    log.warn(
        "[CONFIRM-NACK] messageId={} attempt={} cause={}",
        messageId,
        pending.get().attemptCount(),
        cause);

    if (pending.get().attemptCount() < MAX_CONFIRM_RETRIES) {
      long delayMs = backoffDelay(pending.get().attemptCount());
      log.info("[CONFIRM-RETRY] Scheduling retry in {}ms for messageId={}", delayMs, messageId);
      taskScheduler.schedule(
          () -> {
            try {
              send(
                  pending.get().exchange(),
                  pending.get().routingKey(),
                  pending.get().payload(),
                  pending.get().attemptCount() + 1,
                  pending.get().firstAttemptAt());
            } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
            }
          },
          Instant.now().plusMillis(delayMs));
    } else {
      log.error("[CONFIRM-DLQ] Max retries reached for messageId={}", messageId);
      dlqPublisher.sendToConfirmDLQ(pending, "NACK", cause);
    }

    pendingMessageStore.remove(messageId);
  }

  private void handleReturn(ReturnedMessage returned) {
    Message message = returned.getMessage();
    String messageId = message.getMessageProperties().getMessageId();
    int attempt = getAttemptHeader(message);

    log.warn(
        "[RETURN] messageId={} replyCode={} replyText={} exchange={} routingKey={}",
        messageId,
        returned.getReplyCode(),
        returned.getReplyText(),
        returned.getExchange(),
        returned.getRoutingKey());

    if (attempt < MAX_RETURN_RETRIES) {
      taskScheduler.schedule(
          () -> {
            message.getMessageProperties().setMessageId(messageId);
            message.getMessageProperties().setHeader("x-attempt", attempt + 1);
            rabbitTemplate.send(
                returned.getExchange(),
                returned.getRoutingKey(),
                message,
                new CorrelationData(messageId));
          },
          Instant.now().plusMillis(5000));
    } else {
      log.error(
          "[RETURN-DLQ] Unroutable after retry — sending to return DLQ. messageId={}", messageId);
      dlqPublisher.sendToReturnDLQ(returned);
      pendingMessageStore.remove(messageId);
    }
  }

  private long backoffDelay(int attempt) {
    return (long) Math.pow(4, attempt) * 1000L;
  }

  private int getAttemptHeader(Message message) {
    Object attempt = message.getMessageProperties().getHeader("x-attempt");
    return attempt == null ? 1 : (int) attempt;
  }
}
