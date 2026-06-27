package com.project.hearmeout_backend.notification_service.services;

import com.project.hearmeout_backend.authentication_service.service.implementation.EmailServiceImpl;
import com.project.hearmeout_backend.common_lib.event_dto.PasswordResetOtpEvent;
import com.project.hearmeout_backend.common_lib.event_dto.UserRegisteredEvent;
import com.project.hearmeout_backend.common_lib.event_dto.VerificationOtpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageListener {

  private final EmailServiceImpl emailServiceImpl;

  @KafkaListener(topics = "userRegisteredEvent", groupId = "welcome-mail-v2")
  public void userRegisteredEventListener(UserRegisteredEvent request) {
    log.info("Initiate Welcome Mail Sender for: {}", request.email());

    emailServiceImpl.sendWelcomeMail(request.email(), request.username());
  }

  @KafkaListener(topics = "passwordResetOtpEvent", groupId = "pass-reset-mail-v2")
  public void passwordResetOtpEventListener(PasswordResetOtpEvent request) {
    log.info("Initiate password reset mail sender for: {}", request.email());

    emailServiceImpl.sendPasswordResetMail(request.email());
  }

  @KafkaListener(topics = "verificationOtpEvent", groupId = "verification-mail-v2")
  public void verificationOtpEventListener(VerificationOtpEvent request) {
    log.info("Initiate account verification mail sender for: {}", request.email());

    emailServiceImpl.sendAccountVerificationMail(request.email());
  }
}
