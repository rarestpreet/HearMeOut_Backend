package com.project.hearmeout_backend.notification_service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.hearmeout_backend.authentication_service.service.implementation.EmailServiceImpl;
import com.project.hearmeout_backend.common_lib.event_dto.PasswordResetOtpEvent;
import com.project.hearmeout_backend.common_lib.event_dto.UserRegisteredEvent;
import com.project.hearmeout_backend.common_lib.event_dto.VerificationOtpEvent;
import com.project.hearmeout_backend.common_lib.service.implementation.UtilServiceImpl;
import com.project.hearmeout_backend.notification_service.config.RabbitMQConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class NotificationConsumersTest {

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

  @Mock private EmailServiceImpl emailService;
  @Mock private UtilServiceImpl utilService;

  @Mock private RedisTemplate<String, String> redisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;

  @BeforeEach
  void setUp() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(anyString(), anyString(), any())).thenReturn(true);
  }

  @Test
  void testWelcomeMailConsumer() throws JsonProcessingException {
    UserRegisteredEvent payload = new UserRegisteredEvent("test@example.com", "testUser");
    String jsonPayload = objectMapper.writeValueAsString(payload);

    Message message =
        MessageBuilder.withBody(jsonPayload.getBytes())
            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
            .build();

    // Act
    rabbitTemplate.send(
        RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.WELCOME_EMAIL_ROUTING_KEY, message);

    // Assert
    verify(emailService, timeout(5000).times(1)).sendWelcomeMail("test@example.com", "testUser");
  }

  @Test
  void testVerificationOtpConsumer() throws JsonProcessingException {
    VerificationOtpEvent payload = new VerificationOtpEvent("verify@example.com");
    String jsonPayload = objectMapper.writeValueAsString(payload);

    when(utilService.handleAccountVerificationOtp("verify@example.com")).thenReturn(123456);

    Message message =
        MessageBuilder.withBody(jsonPayload.getBytes())
            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
            .build();

    // Act
    rabbitTemplate.send(
        RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.VERIFICATION_EMAIL_ROUTING_KEY, message);

    // Assert
    verify(emailService, timeout(5000).times(1))
        .sendAccountVerificationMail("verify@example.com", 123456);
  }

  @Test
  void testPasswordResetOtpConsumer() throws JsonProcessingException {
    PasswordResetOtpEvent payload = new PasswordResetOtpEvent("reset@example.com");
    String jsonPayload = objectMapper.writeValueAsString(payload);

    when(utilService.handlePasswordResetOtp("reset@example.com")).thenReturn(654321);

    Message message =
        MessageBuilder.withBody(jsonPayload.getBytes())
            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
            .build();

    // Act
    rabbitTemplate.send(
        RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.PASSWORD_RESET_EMAIL_ROUTING_KEY, message);

    // Assert
    verify(emailService, timeout(5000).times(1)).sendPasswordResetMail("reset@example.com", 654321);
  }
}
