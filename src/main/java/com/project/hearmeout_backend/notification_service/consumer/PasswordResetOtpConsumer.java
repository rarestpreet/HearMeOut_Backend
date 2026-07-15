package com.project.hearmeout_backend.notification_service.consumer;

import com.project.hearmeout_backend.authentication_service.service.implementation.EmailServiceImpl;
import com.project.hearmeout_backend.common_lib.event_dto.PasswordResetOtpEvent;
import com.project.hearmeout_backend.notification_service.config.RabbitMQConfig;
import com.project.hearmeout_backend.notification_service.infra.ConsumerRetryDispatcher;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetOtpConsumer {

  private final EmailServiceImpl emailService;
  private final ConsumerRetryDispatcher retryDispatcher;
  private final RedisTemplate<String, String> redisTemplate;

  @RabbitListener(queues = RabbitMQConfig.PASSWORD_RESET_EMAIL_QUEUE, ackMode = "MANUAL")
  public void consume(Message message, Channel channel, @Payload PasswordResetOtpEvent payload)
      throws IOException {
    String messageId = message.getMessageProperties().getMessageId();
    long deliveryTag = message.getMessageProperties().getDeliveryTag();

    log.info(
        "[CONSUMER] Received password reset OTP message. messageId={} email={}",
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

      emailService.sendPasswordResetMail(payload.email(), payload.otp());

      redisTemplate.delete(messageId);

      channel.basicAck(deliveryTag, false);
      log.info("[CONSUMER] Successfully processed. messageId={}", messageId);
    } catch (Exception e) {
      retryDispatcher.dispatch(message, channel, e, RabbitMQConfig.PASSWORD_RESET_EMAIL_DLQ);
      redisTemplate.delete(messageId);
    }
  }
}
