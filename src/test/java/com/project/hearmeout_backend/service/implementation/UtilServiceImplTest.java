package com.project.hearmeout_backend.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.common_lib.service.implementation.UtilServiceImpl;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import java.security.SecureRandom;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UtilServiceImplTest {

  @Mock private UserServiceImpl userServiceImpl;

  @Mock private UserRepository userRepo;

  @Mock private BCryptPasswordEncoder passwordEncoder;

  @Mock private StringRedisTemplate redisOperator;

  @Mock private ValueOperations<String, String> valueOperations;

  @Spy @InjectMocks private UtilServiceImpl utilService;

  private Map<String, User> userList;
  private Integer otp;

  @BeforeEach
  public void setUp() {
    userList =
        Map.of(
            "test1@gmail.com", User.builder().username("test1").build(),
            "test2@gmail.com", User.builder().username("test2").build(),
            "test3@gmail.com", User.builder().username("test3").build());
    otp = 100000 + new SecureRandom().nextInt(900000);
  }

  @ParameterizedTest
  @ValueSource(strings = {"test1@gmail.com", "test2@gmail.com", "test4@gmail.com"})
  public void handlePasswordResetOtp(String email) {
    // Arrange
    boolean userExists = userList.containsKey(email);
    when(userRepo.existsByEmail(email)).thenReturn(userExists);

    if (!userExists) {
      // Service throws UserNotFoundException for non-existent email
      UserNotFoundException exception =
          assertThrows(
              UserNotFoundException.class, () -> utilService.handlePasswordResetOtp(email));

      assertEquals("User not found with email: " + email, exception.getMessage());
      return;
    }

    doReturn(otp).when(utilService).generateOtp();
    when(redisOperator.opsForValue()).thenReturn(valueOperations);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedOtp");

    // Act
    Integer generatedOtp = utilService.handlePasswordResetOtp(email);

    // Assert
    assertEquals(otp, generatedOtp);

    verify(redisOperator, atLeastOnce()).opsForValue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"test1@gmail.com", "test2@gmail.com", "test4@gmail.com"})
  public void handleAccountVerificationOtp(String email) {
    // Arrange
    User registeredUser = userList.get(email);

    if (registeredUser == null) {
      when(userServiceImpl.checkAndGetUserByEmail(email)).thenReturn(null);

      assertThrows(
          NullPointerException.class, () -> utilService.handleAccountVerificationOtp(email));
      return;
    }

    // User exists and is not verified
    when(userServiceImpl.checkAndGetUserByEmail(email)).thenReturn(registeredUser);

    doReturn(otp).when(utilService).generateOtp();
    when(redisOperator.opsForValue()).thenReturn(valueOperations);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedOtp");

    // Act
    Integer generatedOtp = utilService.handleAccountVerificationOtp(email);

    // Assert
    assertEquals(otp, generatedOtp);

    verify(redisOperator, atLeastOnce()).opsForValue();
  }
}
