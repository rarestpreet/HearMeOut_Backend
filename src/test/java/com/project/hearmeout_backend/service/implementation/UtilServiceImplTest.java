package com.project.hearmeout_backend.service.implementation;

import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.authentication_service.service.implementation.UtilServiceImpl;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UtilServiceImplTest {

    @Mock
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepo;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Spy
    @InjectMocks
    private UtilServiceImpl utilService;

    private Map<String, User> userList;
    private Integer otp;

    @BeforeEach
    public void setUp() {
        userList = Map.of(
                "test1@gmail.com", User.builder().username("test1").build(),
                "test2@gmail.com", User.builder().username("test2").build(),
                "test3@gmail.com", User.builder().username("test3").build()
        );
        otp = 100000 + new SecureRandom().nextInt(900000);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "test1@gmail.com",
                    "test2@gmail.com",
                    "test4@gmail.com"
            }
    )
    public void handlePasswordResetOtp(String email) {
        //Arrange
        User registeredUser = userList.get(email);
        when(userService.checkAndGetUserByEmail(email))
                .thenReturn(userList.get(email));

        if (registeredUser == null || userList.get(email) == null) {
            assertThrows(NullPointerException.class, () ->
                    utilService.handlePasswordResetOtp(email)
            );
            return;
        }

        doReturn(otp).
                when(utilService).generateOtp();
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedOtp");

        //Act
        Integer generatedOtp = utilService.handlePasswordResetOtp(email);

        //Assert
        assertEquals(
                otp,
                generatedOtp
        );
        assertTrue(
                System.currentTimeMillis() < registeredUser.getPasswordOtpExpireAt()
        );
        assertEquals(
                passwordEncoder.encode(otp.toString()),
                registeredUser.getPasswordChangeOtp()
        );

        verify(userRepo).save(registeredUser);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "test1@gmail.com",
                    "test2@gmail.com",
                    "test4@gmail.com"
            }
    )
    public void handleAccountVerificationOtp(String email) {
        //Arrange
        User registeredUser = userList.get(email);
        when(userService.checkAndGetUserByEmail(email))
                .thenReturn(userList.get(email));

        if (registeredUser == null || userList.get(email) == null) {
            assertThrows(NullPointerException.class, () ->
                    utilService.handleAccountVerificationOtp(email)
            );
            return;
        }

        doReturn(otp)
                .when(utilService).generateOtp();
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedOtp");

        //Act
        Integer generatedOtp = utilService.handleAccountVerificationOtp(email);

        //Assert
        assertEquals(
                otp,
                generatedOtp
        );
        assertTrue(
                System.currentTimeMillis() < registeredUser.getEmailVerifyOtpExpireAt()
        );
        assertEquals(
                passwordEncoder.encode(otp.toString()),
                registeredUser.getEmailVerifyOtp()
        );

        verify(userRepo)
                .save(registeredUser);
    }
}
