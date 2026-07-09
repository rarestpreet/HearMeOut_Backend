package com.project.hearmeout_backend.notification_service.consumer;

import com.project.hearmeout_backend.notification_service.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerDLQListener {

  @RabbitListener(queues = RabbitMQConfig.VERIFICATION_EMAIL_DLQ, ackMode = "MANUAL")
  public void onVerificationDLQ(Message message, Channel channel) throws IOException {
    handle(message, channel, RabbitMQConfig.VERIFICATION_EMAIL_DLQ);
  }

  @RabbitListener(queues = RabbitMQConfig.PASSWORD_RESET_EMAIL_DLQ, ackMode = "MANUAL")
  public void onPasswordResetDLQ(Message message, Channel channel) throws IOException {
    handle(message, channel, RabbitMQConfig.PASSWORD_RESET_EMAIL_DLQ);
  }

  @RabbitListener(queues = RabbitMQConfig.WELCOME_EMAIL_DLQ, ackMode = "MANUAL")
  public void onWelcomeDLQ(Message message, Channel channel) throws IOException {
    handle(message, channel, RabbitMQConfig.WELCOME_EMAIL_DLQ);
  }

  private void handle(Message message, Channel channel, String dlqName) throws IOException {
    String body = new String(message.getBody());
    log.error("[DLQ-LISTENER] Message in {}:\n{}", dlqName, body);

    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
  }
}
