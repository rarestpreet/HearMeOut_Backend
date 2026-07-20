package com.project.hearmeout_backend.common_lib.service.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.hearmeout_backend.common_lib.event_dto.PasswordResetOtpEvent;
import com.project.hearmeout_backend.common_lib.event_dto.VerificationOtpEvent;
import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.notification_service.config.CustomRabbitTemplate;
import com.project.hearmeout_backend.notification_service.config.RabbitMQConfig;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UtilServiceImpl {

  private final BCryptPasswordEncoder passwordEncoder;
  private final StringRedisTemplate redisOperator;
  private final UserRepository userRepo;
  private final UserServiceImpl userServiceImpl;
  private final CustomRabbitTemplate rabbitTemplate;

  public Integer generateOtp() {
    SecureRandom random = new SecureRandom();
    return 100000 + random.nextInt(900000);
  }

  @Transactional
  public void handlePasswordResetOtp(String email) throws JsonProcessingException {
    if (!userRepo.existsByEmail(email)) {
      throw new UserNotFoundException("User not found with email: " + email);
    }

    Integer otp = generateOtp();

    String passKey = "pass_reset_otp:".concat(email);
    redisOperator
        .opsForHash()
        .put(passKey, "otp_value", Objects.requireNonNull(passwordEncoder.encode(otp.toString())));
    redisOperator.opsForHash().put(passKey, "remaining_attempts", "5");
    redisOperator.expire(passKey, Duration.ofMinutes(20));
    redisOperator
        .opsForValue()
        .set("pass_reset_cooldown:".concat(email), "", Duration.ofMinutes(1));

    rabbitTemplate.send(
        RabbitMQConfig.EMAIL_EXCHANGE,
        RabbitMQConfig.PASSWORD_RESET_EMAIL_ROUTING_KEY,
        new PasswordResetOtpEvent(email, otp));
  }

  @Transactional
  public void handleAccountVerificationOtp(String email) throws JsonProcessingException {
    User user = userServiceImpl.checkAndGetUserByEmail(email);
    if (user.isAccountVerified()) {
      throw new InvalidOperationException("User already verified with email: " + email);
    }
    Integer otp = generateOtp();

    String verifyKey = "account_verify_otp:".concat(email);
    redisOperator
        .opsForHash()
        .put(
            verifyKey, "otp_value", Objects.requireNonNull(passwordEncoder.encode(otp.toString())));
    redisOperator.opsForHash().put(verifyKey, "remaining_attempts", "5");
    redisOperator.expire(verifyKey, Duration.ofHours(12));
    redisOperator
        .opsForValue()
        .set("account_verify_cooldown:".concat(email), "", Duration.ofMinutes(1));

    rabbitTemplate.send(
        RabbitMQConfig.EMAIL_EXCHANGE,
        RabbitMQConfig.VERIFICATION_EMAIL_ROUTING_KEY,
        new VerificationOtpEvent(email, otp));
  }
}
