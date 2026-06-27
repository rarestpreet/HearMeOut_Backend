package com.project.hearmeout_backend.authentication_service.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl {

  private final JavaMailSender mailSender;
  private final UtilServiceImpl utilServiceImpl;

  public void sendWelcomeMail(String receiverMail, String username) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();

      message.setTo(receiverMail);
      message.setSubject("Welcome to the community");
      message.setText("Hello " + username + ", thanks for joining our community");

      mailSender.send(message);
    } catch (Exception e) {
      log.warn("Error sending welcome mail {}", e.getMessage());
      throw new RuntimeException(
          "Error sending welcome mail for %s: %s".formatted(receiverMail, e.getMessage()));
    }
    log.info("Welcome mail sent successfully to: {}", receiverMail);
  }

  public void sendPasswordResetMail(String receiverMail) {
    Integer otp = utilServiceImpl.handlePasswordResetOtp(receiverMail);

    try {
      SimpleMailMessage message = new SimpleMailMessage();

      message.setTo(receiverMail);
      message.setSubject("Reset Password");
      message.setText("Otp to reset your password is " + otp + ", it will expire in 15 minutes");

      mailSender.send(message);
    } catch (RuntimeException e) {
      log.error("Error sending password reset mail ", e);
      throw new RuntimeException(
          "Error sending password reset mail to %s: %s".formatted(receiverMail, e.getMessage()));
    }
    log.info("Password reset mail sent successfully to: {}", receiverMail);
  }

  public void sendAccountVerificationMail(String receiverMail) {
    Integer otp = utilServiceImpl.handleAccountVerificationOtp(receiverMail);

    try {
      SimpleMailMessage message = new SimpleMailMessage();

      message.setTo(receiverMail);
      message.setSubject("Account verification");
      message.setText("Otp to verify your account is " + otp + ", it will expire in 12 hours");

      mailSender.send(message);
    } catch (Exception e) {
      log.error("Error sending email verification mail ", e);
      throw new RuntimeException(
          "Error sending account verification mail to %s: %s"
              .formatted(receiverMail, e.getMessage()));
    }
    log.info("Account verification mail sent successfully to: {}", receiverMail);
  }
}
