package com.project.hearmeout_backend.notification_service.consumer;

import com.project.hearmeout_backend.notification_service.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// notification_service/consumer/PublisherDLQListener.java
@Slf4j
@Component
@RequiredArgsConstructor
public class PublisherDLQListener {

  @RabbitListener(queues = RabbitMQConfig.PUBLISHER_CONFIRM_DLQ_QUEUE, ackMode = "MANUAL")
  public void onConfirmDLQ(Message message, Channel channel) throws IOException {
    handle(message, channel, "publisher.confirm.dlq");
  }

  @RabbitListener(queues = RabbitMQConfig.PUBLISHER_RETURN_DLQ_QUEUE, ackMode = "MANUAL")
  public void onReturnDLQ(Message message, Channel channel) throws IOException {
    handle(message, channel, "publisher.return.dlq");
  }

  private void handle(Message message, Channel channel, String dlqName) throws IOException {
    String body = new String(message.getBody());
    log.error("[PUBLISHER-DLQ] Message in {}:\n{}", dlqName, body);

    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
  }
}
