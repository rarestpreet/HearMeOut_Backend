package com.project.hearmeout_backend.notification_service.consumer;

import com.project.hearmeout_backend.authentication_service.service.implementation.EmailServiceImpl;
import com.project.hearmeout_backend.common_lib.event_dto.UserRegisteredEvent;
import com.project.hearmeout_backend.notification_service.config.RabbitMQConfig;
import com.project.hearmeout_backend.notification_service.infra.ConsumerRetryDispatcher;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelcomeMailConsumer {

  private final EmailServiceImpl emailService;
  private final ConsumerRetryDispatcher retryDispatcher;
  private final StringRedisTemplate redisTemplate;

  @RabbitListener(queues = RabbitMQConfig.WELCOME_EMAIL_QUEUE, ackMode = "MANUAL")
  public void consume(Message message, Channel channel, @Payload UserRegisteredEvent payload)
      throws IOException {
    String messageId = message.getMessageProperties().getMessageId();
    long deliveryTag = message.getMessageProperties().getDeliveryTag();

    log.info(
        "[CONSUMER] Received welcome mail message. messageId={} email={}",
        messageId,
        payload.email());

    try {
      Boolean isAlreadyProcessed =
          redisTemplate.opsForValue().setIfAbsent(messageId, "1", Duration.ofMinutes(10));

      if (!Boolean.TRUE.equals(isAlreadyProcessed)) {
        log.info("[CONSUMER] Duplicate message, skipping. messageId={}", messageId);
        channel.basicAck(deliveryTag, false);
        return;
      }

      emailService.sendWelcomeMail(payload.email(), payload.username());
      redisTemplate.delete(messageId);

      channel.basicAck(deliveryTag, false);
      log.info("[CONSUMER] Successfully processed. messageId={}", messageId);

    } catch (Exception e) {
      retryDispatcher.dispatch(message, channel, e, RabbitMQConfig.WELCOME_EMAIL_DLQ);
      redisTemplate.delete(messageId);
    }
  }
}
