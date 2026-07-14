package com.project.hearmeout_backend.notification_service.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.hearmeout_backend.notification_service.config.RabbitMQConfig;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerDLQPublisher {

  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  public void publish(
      Message originalMessage, Exception exception, long attemptCount, String dlqRoutingKey) {
    StringWriter sw = new StringWriter();
    exception.printStackTrace(new PrintWriter(sw));

    Map<String, Object> dlqPayload = new LinkedHashMap<>();
    dlqPayload.put("failureType", "CONSUMER_PROCESSING_FAILURE");
    dlqPayload.put("messageId", originalMessage.getMessageProperties().getMessageId());
    dlqPayload.put("sourceQueue", originalMessage.getMessageProperties().getConsumerQueue());
    dlqPayload.put("payload", deserializePayload(originalMessage));
    dlqPayload.put("exceptionType", exception.getClass().getName());
    dlqPayload.put("exceptionMessage", exception.getMessage());
    dlqPayload.put("stackTrace", sw.toString());
    dlqPayload.put("attemptCount", attemptCount);
    dlqPayload.put("finalFailureAt", Instant.now().toString());

    rabbitTemplate.convertAndSend(RabbitMQConfig.DLQ_EXCHANGE, dlqRoutingKey, dlqPayload);
    log.error(
        "[CONSUMER-DLQ] Published to {}. messageId={} exception={}",
        dlqRoutingKey,
        originalMessage.getMessageProperties().getMessageId(),
        exception.getClass().getSimpleName());
  }

  private Object deserializePayload(Message message) {
    try {
      return objectMapper.readValue(message.getBody(), Object.class);
    } catch (Exception e) {
      return new String(message.getBody());
    }
  }
}
