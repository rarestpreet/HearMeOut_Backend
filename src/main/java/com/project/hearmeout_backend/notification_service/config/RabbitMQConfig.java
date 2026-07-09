package com.project.hearmeout_backend.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String EMAIL_EXCHANGE = "email.exchange";
  public static final String DLQ_EXCHANGE = "dlq.exchange";
  public static final String RETRY_EXCHANGE = "retry.exchange";

  public static final String RETRY_FAST_QUEUE = "retry.fast.queue";
  public static final String RETRY_FAST_ROUTING_KEY = "retry.fast";
  public static final int RETRY_FAST_TTL_MS = 5_000;

  public static final String RETRY_SLOW_QUEUE = "retry.slow.queue";
  public static final String RETRY_SLOW_ROUTING_KEY = "retry.slow";
  public static final int RETRY_SLOW_TTL_MS = 30_000;

  public static final String WELCOME_EMAIL_QUEUE = "email.queue.welcome";
  public static final String WELCOME_EMAIL_ROUTING_KEY = "email.welcome";

  public static final String VERIFICATION_EMAIL_QUEUE = "email.queue.verification-otp";
  public static final String VERIFICATION_EMAIL_ROUTING_KEY = "email.verification-otp";

  public static final String PASSWORD_RESET_EMAIL_QUEUE = "email.queue.password-reset-otp";
  public static final String PASSWORD_RESET_EMAIL_ROUTING_KEY = "email.password-reset";

  public static final String PUBLISHER_CONFIRM_DLQ_QUEUE = "publisher.confirm.dlq";
  public static final String PUBLISHER_CONFIRM_DLQ_ROUTING_KEY = "publisher.confirm.dlq";

  public static final String PUBLISHER_RETURN_DLQ_QUEUE = "publisher.return.dlq";
  public static final String PUBLISHER_RETURN_DLQ_ROUTING_KEY = "publisher.return.dlq";

  public static final String WELCOME_EMAIL_DLQ = "consumer.dlq.welcome";
  public static final String VERIFICATION_EMAIL_DLQ = "consumer.dlq.verification-otp";
  public static final String PASSWORD_RESET_EMAIL_DLQ = "consumer.dlq.password-reset-otp";

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new JacksonJsonMessageConverter();
  }

  @Bean
  public DirectExchange retryExchange() {
    return new DirectExchange(RETRY_EXCHANGE, true, false);
  }

  @Bean
  public DirectExchange emailExchange() {
    return new DirectExchange(EMAIL_EXCHANGE);
  }

  @Bean
  public DirectExchange dlqExchange() {
    return new DirectExchange(DLQ_EXCHANGE, true, false);
  }

  @Bean
  public Queue retryFastQueue() {
    return QueueBuilder.durable(RETRY_FAST_QUEUE)
        .withArgument("x-message-ttl", RETRY_FAST_TTL_MS)
        .withArgument("x-dead-letter-exchange", EMAIL_EXCHANGE)
        .build();
  }

  @Bean
  public Binding retryFastBinding() {
    return BindingBuilder.bind(retryFastQueue()).to(retryExchange()).with(RETRY_FAST_ROUTING_KEY);
  }

  @Bean
  public Queue retrySlowQueue() {
    return QueueBuilder.durable(RETRY_SLOW_QUEUE)
        .withArgument("x-message-ttl", RETRY_SLOW_TTL_MS)
        .withArgument("x-dead-letter-exchange", EMAIL_EXCHANGE)
        .build();
  }

  @Bean
  public Binding retrySlowBinding() {
    return BindingBuilder.bind(retrySlowQueue()).to(retryExchange()).with(RETRY_SLOW_ROUTING_KEY);
  }

  @Bean
  public Queue welcomeQueue() {
    return QueueBuilder.durable(WELCOME_EMAIL_QUEUE)
        .withArgument("x-dead-letter-exchange", RETRY_EXCHANGE)
        .build();
  }

  @Bean
  public Binding welcomeBinding(Queue welcomeQueue, DirectExchange emailExchange) {
    return BindingBuilder.bind(welcomeQueue).to(emailExchange).with(WELCOME_EMAIL_ROUTING_KEY);
  }

  @Bean
  public Queue verificationQueue() {
    return QueueBuilder.durable(VERIFICATION_EMAIL_QUEUE)
        .withArgument("x-dead-letter-exchange", RETRY_EXCHANGE)
        .build();
  }

  @Bean
  public Binding verificationBinding(Queue verificationQueue, DirectExchange emailExchange) {
    return BindingBuilder.bind(verificationQueue)
        .to(emailExchange)
        .with(VERIFICATION_EMAIL_ROUTING_KEY);
  }

  @Bean
  public Queue passwordResetQueue() {
    return QueueBuilder.durable(PASSWORD_RESET_EMAIL_QUEUE)
        .withArgument("x-dead-letter-exchange", RETRY_EXCHANGE)
        .build();
  }

  @Bean
  public Binding passwordResetBinding(Queue passwordResetQueue, DirectExchange emailExchange) {
    return BindingBuilder.bind(passwordResetQueue)
        .to(emailExchange)
        .with(PASSWORD_RESET_EMAIL_ROUTING_KEY);
  }

  @Bean
  public Queue publisherConfirmDLQ() {
    return QueueBuilder.durable(PUBLISHER_CONFIRM_DLQ_QUEUE).build();
  }

  @Bean
  public Binding publisherConfirmDLQBinding(Queue publisherConfirmDLQ, DirectExchange dlqExchange) {
    return BindingBuilder.bind(publisherConfirmDLQ)
        .to(dlqExchange)
        .with(PUBLISHER_CONFIRM_DLQ_ROUTING_KEY);
  }

  @Bean
  public Queue publisherReturnDLQ() {
    return QueueBuilder.durable(PUBLISHER_RETURN_DLQ_QUEUE).build();
  }

  @Bean
  public Binding publisherReturnDLQBinding(Queue publisherReturnDLQ, DirectExchange dlqExchange) {
    return BindingBuilder.bind(publisherReturnDLQ)
        .to(dlqExchange)
        .with(PUBLISHER_RETURN_DLQ_ROUTING_KEY);
  }

  @Bean
  public Queue welcomeDLQ() {
    return QueueBuilder.durable(WELCOME_EMAIL_DLQ).build();
  }

  @Bean
  public Binding welcomeDLQBinding() {
    return BindingBuilder.bind(welcomeDLQ()).to(dlqExchange()).with(WELCOME_EMAIL_DLQ);
  }

  @Bean
  public Queue verificationDLQ() {
    return QueueBuilder.durable(VERIFICATION_EMAIL_DLQ).build();
  }

  @Bean
  public Binding verificationDLQBinding() {
    return BindingBuilder.bind(verificationDLQ()).to(dlqExchange()).with(VERIFICATION_EMAIL_DLQ);
  }

  @Bean
  public Queue passwordResetDLQ() {
    return QueueBuilder.durable(PASSWORD_RESET_EMAIL_DLQ).build();
  }

  @Bean
  public Binding passwordResetDLQBinding() {
    return BindingBuilder.bind(passwordResetDLQ()).to(dlqExchange()).with(PASSWORD_RESET_EMAIL_DLQ);
  }
}
