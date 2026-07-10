package com.project.hearmeout_backend.notification_service.infra;

import com.project.hearmeout_backend.notification_service.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.net.ConnectException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerRetryDispatcher {

  private static final int MAX_RETRIES = 3;

  private final RabbitTemplate rabbitTemplate;
  private final RetryCountExtractor retryCountExtractor;
  private final ConsumerDLQPublisher dlqPublisher;

  public void dispatch(Message message, Channel channel, Exception exception, String dlqRoutingKey)
      throws IOException {
    long deliveryTag = message.getMessageProperties().getDeliveryTag();
    long retryCount = retryCountExtractor.getRetryCount(message);
    String messageId = message.getMessageProperties().getMessageId();

    log.warn(
        "[CONSUMER-FAIL] messageId={} attempt={} exception={}",
        messageId,
        retryCount + 1,
        exception.getClass().getSimpleName());

    if (exception instanceof IllegalArgumentException) {
      log.error("[CONSUMER-DLQ] Business exception, no retry. messageId={}", messageId);
      dlqPublisher.publish(message, exception, retryCount + 1, dlqRoutingKey);
      channel.basicAck(deliveryTag, false);
      return;
    }

    // Max retries reached — send to DLQ
    if (retryCount >= MAX_RETRIES) {
      log.error("[CONSUMER-DLQ] Max retries reached. messageId={}", messageId);
      dlqPublisher.publish(message, exception, retryCount + 1, dlqRoutingKey);
      channel.basicAck(deliveryTag, false);
      return;
    }

    // Decide retry tier based on exception type
    String retryRoutingKey = resolveRetryRoutingKey(exception);
    log.info(
        "[CONSUMER-RETRY] Routing to {} queue. messageId={} attempt={}/{}",
        retryRoutingKey,
        messageId,
        retryCount + 1,
        MAX_RETRIES);

    rabbitTemplate.convertAndSend(
        RabbitMQConfig.RETRY_EXCHANGE,
        retryRoutingKey,
        message.getBody(),
        msg -> {
          msg.getMessageProperties().setHeaders(message.getMessageProperties().getHeaders());
          msg.getMessageProperties().setMessageId(messageId);
          msg.getMessageProperties().setContentType("application/json");
          return msg;
        });

    channel.basicAck(deliveryTag, false);
  }

  private String resolveRetryRoutingKey(Exception exception) {
    // Redis failures — fast retry (5s)
    if (exception instanceof RedisConnectionFailureException
        || exception instanceof QueryTimeoutException) {
      return RabbitMQConfig.RETRY_FAST_ROUTING_KEY;
    }

    // Mail server failures — slow retry (30s)
    if (exception instanceof MailException || exception instanceof ConnectException) {
      return RabbitMQConfig.RETRY_SLOW_ROUTING_KEY;
    }

    // Unknown — fast retry, but MAX_RETRIES is low so it hits DLQ quickly
    return RabbitMQConfig.RETRY_FAST_ROUTING_KEY;
  }
}
