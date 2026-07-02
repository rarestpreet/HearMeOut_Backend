package com.project.hearmeout_backend.notification_service.services;

import com.project.hearmeout_backend.authentication_service.service.implementation.EmailServiceImpl;
import com.project.hearmeout_backend.common_lib.event_dto.PasswordResetOtpEvent;
import com.project.hearmeout_backend.common_lib.event_dto.UserRegisteredEvent;
import com.project.hearmeout_backend.common_lib.event_dto.VerificationOtpEvent;
import com.project.hearmeout_backend.notification_service.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQMessageListener {

  private final EmailServiceImpl emailServiceImpl;

  @RabbitListener(queues = RabbitMQConfig.WELCOME_QUEUE)
  public void onUserRegistered(UserRegisteredEvent event) {
    log.info("Initiate Welcome Mail Sender for: {}", event.email());
    emailServiceImpl.sendWelcomeMail(event.email(), event.username());
  }

  @RabbitListener(queues = RabbitMQConfig.PASSWORD_RESET_QUEUE)
  public void onPasswordResetOtp(PasswordResetOtpEvent event) {
    log.info("Initiate password reset mail sender for: {}", event.email());
    emailServiceImpl.sendPasswordResetMail(event.email());
  }

  @RabbitListener(queues = RabbitMQConfig.VERIFICATION_QUEUE)
  public void onVerificationOtp(VerificationOtpEvent event) {
    log.info("Initiate account verification mail sender for: {}", event.email());
    emailServiceImpl.sendAccountVerificationMail(event.email());
  }
}
