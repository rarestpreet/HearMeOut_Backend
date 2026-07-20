package com.project.hearmeout_backend.authentication_service.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.project.hearmeout_backend.authentication_service.dto.request.AccountVerificationRequestDTO;
import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.common_lib.exception.InvalidOtpException;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceImplTest {

  @Mock private UserRepository userRepo;
  @Mock private BCryptPasswordEncoder passwordEncoder;
  @Mock private UserServiceImpl userServiceImpl;
  @Mock private StringRedisTemplate redisOperator;
  @Mock private HashOperations<String, Object, Object> hashOperations;

  @InjectMocks private SecurityServiceImpl securityService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setEmail("test@test.com");
    testUser.setUsername("testUser");
  }

  @Test
  void verifyUserAccount_Success() {
    AccountVerificationRequestDTO dto = new AccountVerificationRequestDTO();
    dto.setOtp("123456");
    String email = testUser.getEmail();
    String key = "account_verify_otp:".concat(email);
    String storedEncodedOtp = "encoded123456";

    when(userServiceImpl.checkAndGetUserByEmail(email)).thenReturn(testUser);
    when(redisOperator.opsForHash()).thenReturn(hashOperations);
    when(hashOperations.get(key, "otp_value")).thenReturn(storedEncodedOtp);
    when(hashOperations.increment(key, "remaining_attempts", -1)).thenReturn(4L);
    when(passwordEncoder.matches("123456", storedEncodedOtp)).thenReturn(true);

    securityService.verifyUserAccount(dto, email);

    verify(redisOperator).delete(key);
    verify(userRepo).save(testUser);
    assertEquals(true, testUser.isAccountVerified());
  }

  @Test
  void verifyUserAccount_ExpiredOtp() {
    AccountVerificationRequestDTO dto = new AccountVerificationRequestDTO();
    dto.setOtp("123456");
    String email = testUser.getEmail();
    String key = "account_verify_otp:".concat(email);

    when(userServiceImpl.checkAndGetUserByEmail(email)).thenReturn(testUser);
    when(redisOperator.opsForHash()).thenReturn(hashOperations);
    when(hashOperations.get(key, "otp_value")).thenReturn(null);

    InvalidOtpException ex =
        assertThrows(
            InvalidOtpException.class, () -> securityService.verifyUserAccount(dto, email));

    assertEquals("Otp expired for account verification, please create a new one.", ex.getMessage());
    verify(hashOperations, never()).increment(anyString(), anyString(), anyLong());
  }

  @Test
  void verifyUserAccount_WrongOtp_DecrementsRemaining() {
    AccountVerificationRequestDTO dto = new AccountVerificationRequestDTO();
    dto.setOtp("123456");
    String email = testUser.getEmail();
    String key = "account_verify_otp:".concat(email);
    String storedEncodedOtp = "encoded654321";

    when(userServiceImpl.checkAndGetUserByEmail(email)).thenReturn(testUser);
    when(redisOperator.opsForHash()).thenReturn(hashOperations);
    when(hashOperations.get(key, "otp_value")).thenReturn(storedEncodedOtp);
    when(hashOperations.increment(key, "remaining_attempts", -1))
        .thenReturn(4L); // still have attempts
    when(passwordEncoder.matches("123456", storedEncodedOtp)).thenReturn(false);

    InvalidOtpException ex =
        assertThrows(
            InvalidOtpException.class, () -> securityService.verifyUserAccount(dto, email));

    assertEquals("Otp for account verification 123456 is not valid", ex.getMessage());
    verify(redisOperator, never()).delete(key); // Shouldn't delete since attempts remain
  }

  @Test
  void verifyUserAccount_MaxAttemptsReached_DeletesOtp() {
    AccountVerificationRequestDTO dto = new AccountVerificationRequestDTO();
    dto.setOtp("123456");
    String email = testUser.getEmail();
    String key = "account_verify_otp:".concat(email);
    String storedEncodedOtp = "encoded654321";

    when(userServiceImpl.checkAndGetUserByEmail(email)).thenReturn(testUser);
    when(redisOperator.opsForHash()).thenReturn(hashOperations);
    when(hashOperations.get(key, "otp_value")).thenReturn(storedEncodedOtp);
    // Return -1 to simulate the 6th attempt (starting from 5, 0 is the 5th failed attempt, -1 means
    // it dropped below 0)
    // Wait, the logic is "if (remaining < 0) ... delete ... throw InvalidOperationException"
    when(hashOperations.increment(key, "remaining_attempts", -1)).thenReturn(-1L);

    InvalidOperationException ex =
        assertThrows(
            InvalidOperationException.class, () -> securityService.verifyUserAccount(dto, email));

    assertEquals("Maximum attempts reached. Otp has been invalidated.", ex.getMessage());
    verify(redisOperator).delete(key);
  }
}
