package com.project.hearmeout_backend.notification_service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.hearmeout_backend.notification_service.config.RabbitMQConfig;
import com.project.hearmeout_backend.notification_service.consumer.ConsumerDLQListener;
import com.project.hearmeout_backend.notification_service.consumer.PublisherDLQListener;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class DLQListenersTest {

  @Container
  static RabbitMQContainer rabbitMQContainer =
      new RabbitMQContainer("rabbitmq:3.11-management").withExposedPorts(5672, 15672);

  @DynamicPropertySource
  static void registerRabbitMQProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
    registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
    registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
    registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
  }

  @Autowired private RabbitTemplate rabbitTemplate;
  @Autowired private ObjectMapper objectMapper;

  @MockitoSpyBean private ConsumerDLQListener consumerDLQListener;
  @MockitoSpyBean private PublisherDLQListener publisherDLQListener;

  @MockitoBean private StringRedisTemplate stringRedisTemplate;

  @Test
  void testConsumerDLQListener() throws IOException {
    // Act: Send directly to the DLQ queue for Consumer
    rabbitTemplate.convertAndSend(RabbitMQConfig.VERIFICATION_EMAIL_DLQ, "dead letter payload");

    // Assert: Spy should verify that handle was called
    verify(consumerDLQListener, timeout(5000).times(1))
        .onVerificationDLQ(any(), any(Channel.class));
  }

  @Test
  void testPublisherDLQListener_Confirm() throws IOException {
    // Act: Send directly to Publisher DLQ queue (Confirm)
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.PUBLISHER_CONFIRM_DLQ_QUEUE, "confirm dead payload");

    // Assert
    verify(publisherDLQListener, timeout(5000).times(1)).onConfirmDLQ(any(), any());
  }

  @Test
  void testPublisherDLQListener_Return() throws IOException {
    // Act: Send directly to Publisher DLQ queue (Return)
    rabbitTemplate.convertAndSend(RabbitMQConfig.PUBLISHER_RETURN_DLQ_QUEUE, "return dead payload");

    // Assert
    verify(publisherDLQListener, timeout(5000).times(1)).onReturnDLQ(any(), any());
  }
}
