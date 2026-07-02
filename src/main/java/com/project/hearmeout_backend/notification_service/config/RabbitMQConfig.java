package com.project.hearmeout_backend.notification_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String EMAIL_EXCHANGE = "email.exchange";

  public static final String WELCOME_QUEUE = "email.queue.welcome";
  public static final String WELCOME_ROUTING_KEY = "email.welcome";

  public static final String VERIFICATION_QUEUE = "email.queue.verification-otp";
  public static final String VERIFICATION_ROUTING_KEY = "email.verification-otp";

  public static final String PASSWORD_RESET_QUEUE = "email.queue.password-reset-otp";
  public static final String PASSWORD_RESET_ROUTING_KEY = "email.password-reset";

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new JacksonJsonMessageConverter();
  }

  @Bean
  public DirectExchange emailExchange() {
    return new DirectExchange(EMAIL_EXCHANGE);
  }

  @Bean
  public Queue welcomeQueue() {
    return new Queue(WELCOME_QUEUE, true);
  }

  @Bean
  public Binding welcomeBinding(Queue welcomeQueue, DirectExchange emailExchange) {
    return BindingBuilder.bind(welcomeQueue).to(emailExchange).with(WELCOME_ROUTING_KEY);
  }

  @Bean
  public Queue verificationQueue() {
    return new Queue(VERIFICATION_QUEUE, true);
  }

  @Bean
  public Binding verificationBinding(Queue verificationQueue, DirectExchange emailExchange) {
    return BindingBuilder.bind(verificationQueue).to(emailExchange).with(VERIFICATION_ROUTING_KEY);
  }

  @Bean
  public Queue passwordResetQueue() {
    return new Queue(PASSWORD_RESET_QUEUE, true);
  }

  @Bean
  public Binding passwordResetBinding(Queue passwordResetQueue, DirectExchange emailExchange) {
    return BindingBuilder.bind(passwordResetQueue)
        .to(emailExchange)
        .with(PASSWORD_RESET_ROUTING_KEY);
  }
}
