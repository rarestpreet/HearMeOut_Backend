package com.project.hearmeout_backend.notification_service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.hearmeout_backend.notification_service.config.CustomRabbitTemplate;
import com.project.hearmeout_backend.notification_service.config.RabbitMQConfig;
import com.project.hearmeout_backend.notification_service.infra.PublisherDLQPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class CustomRabbitTemplateTest {

  @Container
  static RabbitMQContainer rabbitMQContainer =
      new RabbitMQContainer("rabbitmq:3.11-management").withExposedPorts(5672, 15672);

  @DynamicPropertySource
  static void registerRabbitMQProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
    registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
    registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
    registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
    // Ensure publisher confirms/returns are enabled so CustomRabbitTemplate works properly
    registry.add("spring.rabbitmq.publisher-confirm-type", () -> "correlated");
    registry.add("spring.rabbitmq.publisher-returns", () -> "true");
  }

  @Autowired private CustomRabbitTemplate customRabbitTemplate;
  @Autowired private RabbitTemplate rabbitTemplate;

  @Mock private PublisherDLQPublisher dlqPublisher;

  @Test
  void testSend_ValidRouting_Success() throws JsonProcessingException {
    // Sending a message to an exchange and routing key that are properly bound
    // The message should be confirmed (ack=true) by RabbitMQ broker
    customRabbitTemplate.send(
        RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.WELCOME_EMAIL_ROUTING_KEY, "valid payload");

    // We expect NO interaction with DLQ Publisher since it succeeded
    // Wait slightly to ensure async confirms don't trigger DLQ
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
    }

    verify(dlqPublisher, org.mockito.Mockito.never())
        .sendToConfirmDLQ(any(), anyString(), anyString());
    verify(dlqPublisher, org.mockito.Mockito.never()).sendToReturnDLQ(any());
  }

  @Test
  void testSend_InvalidRoutingKey_TriggersReturnCallback() throws JsonProcessingException {
    // Sending a message to a valid exchange but an invalid routing key
    // Since publisher returns are enabled, the broker will return the message
    // Our CustomRabbitTemplate should catch this and call publishToReturnDLQ

    customRabbitTemplate.send(
        RabbitMQConfig.EMAIL_EXCHANGE, "invalid.routing.key", "invalid routing payload");

    // Assert that the DLQ publisher was invoked for the returned message
    verify(dlqPublisher, timeout(2000).times(1)).sendToReturnDLQ(any());
  }
}
