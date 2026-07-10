package com.project.hearmeout_backend.notification_service.infra;

import static com.project.hearmeout_backend.notification_service.config.RabbitMQConfig.DLQ_EXCHANGE;

import com.project.hearmeout_backend.notification_service.config.RabbitMQConfig;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublisherDLQPublisher {

  private final RabbitTemplate rabbitTemplate;

  public void sendToConfirmDLQ(
      Optional<PendingMessage> pending, String failureReason, String cause) {
    Map<String, Object> dlqPayload = new LinkedHashMap<>();
    dlqPayload.put("failureType", "PUBLISHER_CONFIRM_NACK");
    dlqPayload.put("messageId", pending.get().messageId());
    dlqPayload.put("exchange", pending.get().exchange());
    dlqPayload.put("routingKey", pending.get().routingKey());
    dlqPayload.put("payload", pending.get().payload());
    dlqPayload.put("failureReason", failureReason);
    dlqPayload.put("brokerCause", cause);
    dlqPayload.put("attemptCount", pending.get().attemptCount());
    dlqPayload.put("firstAttemptAt", pending.get().firstAttemptAt().toString());
    dlqPayload.put("finalFailureAt", Instant.now().toString());

    rabbitTemplate.convertAndSend(
        DLQ_EXCHANGE, RabbitMQConfig.PUBLISHER_CONFIRM_DLQ_ROUTING_KEY, dlqPayload);
    log.error("[DLQ-CONFIRM] Published to confirm DLQ. messageId={}", pending.get().messageId());
  }

  public void sendToReturnDLQ(ReturnedMessage returned) {
    String messageId = returned.getMessage().getMessageProperties().getMessageId();

    Map<String, Object> dlqPayload = new LinkedHashMap<>();
    dlqPayload.put("failureType", "PUBLISHER_RETURN_NO_ROUTE");
    dlqPayload.put("messageId", messageId);
    dlqPayload.put("exchange", returned.getExchange());
    dlqPayload.put("routingKey", returned.getRoutingKey());
    dlqPayload.put("replyCode", returned.getReplyCode());
    dlqPayload.put("replyText", returned.getReplyText());
    dlqPayload.put("finalFailureAt", Instant.now().toString());

    rabbitTemplate.convertAndSend(
        DLQ_EXCHANGE, RabbitMQConfig.PUBLISHER_RETURN_DLQ_ROUTING_KEY, dlqPayload);
    log.error("[DLQ-RETURN] Published to return DLQ. messageId={}", messageId);
  }
}
