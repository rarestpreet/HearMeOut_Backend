package com.project.hearmeout_backend.authentication_service.service.implementation;

import java.net.ConnectException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl {

  private final JavaMailSender mailSender;

  public void sendWelcomeMail(String receiverMail, String username) {
    if (receiverMail == null || receiverMail.isBlank()) {
      throw new IllegalArgumentException("Receiver mail must not be null or blank");
    }

    try {
      SimpleMailMessage message = new SimpleMailMessage();

      message.setTo(receiverMail);
      message.setSubject("Welcome to the community");
      message.setText("Hello " + username + ", thanks for joining our community");

      mailSender.send(message);
      log.info("Welcome mail sent successfully to: {}", receiverMail);

    } catch (MailAuthenticationException e) {
      // Auth failure — misconfigured credentials, no point retrying → go to DLQ
      log.error(
          "[EMAIL] Authentication failure sending welcome mail to {}: {}",
          receiverMail,
          e.getMessage(),
          e);
      throw new IllegalArgumentException(
          "Mail authentication failure for %s: %s".formatted(receiverMail, e.getMessage()), e);

    } catch (MailSendException e) {
      // Could be a connection refused / SMTP timeout — wraps ConnectException
      Throwable cause = e.getCause();
      if (cause instanceof ConnectException ce) {
        log.warn(
            "[EMAIL] SMTP connection failure sending welcome mail to {}: {}",
            receiverMail,
            ce.getMessage());
        throw new MailSendException(
            "SMTP connection failure sending welcome mail to %s".formatted(receiverMail), ce);
      }
      // Other send failures (e.g. bad address format at SMTP level) — slow retry
      log.warn(
          "[EMAIL] Mail send failure for welcome mail to {}: {}", receiverMail, e.getMessage());
      throw e;

    } catch (MailException e) {
      // Remaining MailException subtypes — slow retry
      log.warn("[EMAIL] Mail failure sending welcome mail to {}: {}", receiverMail, e.getMessage());
      throw e;
    }
  }

  public void sendPasswordResetMail(String receiverMail, Integer otp) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();

      message.setTo(receiverMail);
      message.setSubject("Reset Password");
      message.setText("Otp to reset your password is " + otp + ", it will expire in 15 minutes");

      mailSender.send(message);
      log.info("Password reset mail sent successfully to: {}", receiverMail);

    } catch (MailAuthenticationException e) {
      log.error(
          "[EMAIL] Authentication failure sending password reset mail to {}: {}",
          receiverMail,
          e.getMessage(),
          e);
      throw new IllegalArgumentException(
          "Mail authentication failure for %s: %s".formatted(receiverMail, e.getMessage()), e);

    } catch (MailSendException e) {
      Throwable cause = e.getCause();
      if (cause instanceof ConnectException ce) {
        log.warn(
            "[EMAIL] SMTP connection failure sending password reset mail to {}: {}",
            receiverMail,
            ce.getMessage());
        throw new MailSendException(
            "SMTP connection failure sending password reset mail to %s".formatted(receiverMail),
            ce);
      }
      log.warn(
          "[EMAIL] Mail send failure for password reset mail to {}: {}",
          receiverMail,
          e.getMessage());
      throw e;

    } catch (MailException e) {
      log.warn(
          "[EMAIL] Mail failure sending password reset mail to {}: {}",
          receiverMail,
          e.getMessage());
      throw e;
    }
  }

  public void sendAccountVerificationMail(String receiverMail, Integer otp) {
    if (receiverMail == null || receiverMail.isBlank()) {
      throw new IllegalArgumentException("Receiver mail must not be null or blank");
    }
    if (otp == null) {
      throw new IllegalArgumentException("OTP must not be null");
    }

    try {
      SimpleMailMessage message = new SimpleMailMessage();

      message.setTo(receiverMail);
      message.setSubject("Account verification");
      message.setText("Otp to verify your account is " + otp + ", it will expire in 12 hours");

      mailSender.send(message);
      log.info("Account verification mail sent successfully to: {}", receiverMail);

    } catch (MailAuthenticationException e) {
      log.error(
          "[EMAIL] Authentication failure sending verification mail to {}: {}",
          receiverMail,
          e.getMessage(),
          e);
      throw new IllegalArgumentException(
          "Mail authentication failure for %s: %s".formatted(receiverMail, e.getMessage()), e);

    } catch (MailSendException e) {
      Throwable cause = e.getCause();
      if (cause instanceof ConnectException ce) {
        log.warn(
            "[EMAIL] SMTP connection failure sending verification mail to {}: {}",
            receiverMail,
            ce.getMessage());
        throw new MailSendException(
            "SMTP connection failure sending verification mail to %s".formatted(receiverMail), ce);
      }
      log.warn(
          "[EMAIL] Mail send failure for verification mail to {}: {}",
          receiverMail,
          e.getMessage());
      throw e;

    } catch (MailException e) {
      log.warn(
          "[EMAIL] Mail failure sending verification mail to {}: {}", receiverMail, e.getMessage());
      throw e;
    }
  }
}
