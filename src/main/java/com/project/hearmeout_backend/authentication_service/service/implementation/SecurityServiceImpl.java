package com.project.hearmeout_backend.authentication_service.service.implementation;

import com.project.hearmeout_backend.authentication_service.config.TokenCookieProperties;
import com.project.hearmeout_backend.authentication_service.dto.request.AccountVerificationRequestDTO;
import com.project.hearmeout_backend.authentication_service.dto.request.LoginRequestDTO;
import com.project.hearmeout_backend.authentication_service.dto.request.PasswordResetRequestDTO;
import com.project.hearmeout_backend.authentication_service.dto.request.RegisterRequestDTO;
import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.common_lib.exception.EmailAlreadyExistException;
import com.project.hearmeout_backend.common_lib.exception.InvalidOtpException;
import com.project.hearmeout_backend.common_lib.exception.TokenInvalidException;
import com.project.hearmeout_backend.common_lib.exception.UserAlreadyExistException;
import com.project.hearmeout_backend.user_service.mapper.UserMapper;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityServiceImpl {

    private final UserRepository userRepo;
    private final AuthenticationManager authManager;
    private final BCryptPasswordEncoder passwordEncoder;
    private final HttpServletRequest httpServletRequest;
    private final JwtServiceImpl jwtServiceImpl;
    private final TokenCookieProperties tokenCookieProperties;
    private final EmailServiceImpl emailServiceImpl;
    private final UserServiceImpl userServiceImpl;
    private final StringRedisTemplate redisOperator;
    private final CustomUserDetailsServiceImpl customUserDetailsServiceImpl;

    @Transactional
    public void createNewUser(RegisterRequestDTO registerRequestDTO)
            throws UserAlreadyExistException, EmailAlreadyExistException {
        if (userRepo.existsByUsernameOrEmail(registerRequestDTO.getUsername(), registerRequestDTO.getEmail())) {
            throw new UserAlreadyExistException("User with similar username or email already exist");
        }

        User user = UserMapper.toProfileEntity(registerRequestDTO,
                passwordEncoder.encode(registerRequestDTO.getPassword()));

        userRepo.save(user);
        log.info("Successfully created new user account for email: {}", registerRequestDTO.getEmail());

        emailServiceImpl.sendWelcomeMail(registerRequestDTO.getEmail(), registerRequestDTO.getUsername());
    }

    public List<ResponseCookie> terminateSession(Cookie[] cookies, String username) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.getContext().setAuthentication(null);

        try {
            redisOperator.delete("refresh_token$" + extractToken(cookies));
        } catch (RuntimeException e) {
            throw new RuntimeException("Unable to delete token on terminateSession() " + e.getMessage());
        }
        log.info("Terminated user session for {}", username);

        return List.of(
                ResponseCookie.from("jwt-token", "")
                        .path("/api/v1")
                        .httpOnly(true)
                        .maxAge(0)
                        .secure(tokenCookieProperties.isSecure())
                        .sameSite(tokenCookieProperties.getSameSite())
                        .build(),
                ResponseCookie.from("refresh-token", "")
                        .path("/api/v1/")
                        .httpOnly(true)
                        .maxAge(0)
                        .secure(tokenCookieProperties.isSecure())
                        .sameSite(tokenCookieProperties.getSameSite())
                        .build()
        );
    }

    public List<ResponseCookie> authenticateUser(LoginRequestDTO loginRequestDTO) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword())
        );

        return handleTokenProcessing(loginRequestDTO.getEmail());
    }

    public ResponseCookie refreshAuthenticationTokens(Cookie[] cookies) {
        String refreshToken = extractToken(cookies);

        if (!refreshToken.isBlank()) {
            try {
                String username = redisOperator.opsForValue().get("refresh_token$" + refreshToken);
                CustomUserDetails currUser = customUserDetailsServiceImpl.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        currUser, null, currUser.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);

                String jwtToken = jwtServiceImpl.generateJwtToken(username);

                return ResponseCookie.from("jwt-token", jwtToken)
                        .path("/api/v1")
                        .httpOnly(true)
                        .secure(tokenCookieProperties.isSecure())
                        .sameSite(tokenCookieProperties.getSameSite())
                        .maxAge(Duration.ofMinutes(20))
                        .build();
            } catch (RuntimeException e) {
                throw new RuntimeException("Unable to refresh authentication token on refreshAuthenticationToken() " + e.getMessage());
            }
        } else {
            throw new TokenInvalidException("Authentication token is invalid, token refresh failed");
        }
    }

    public List<ResponseCookie> handleTokenProcessing(String username) {
        String jwtToken = jwtServiceImpl.generateJwtToken(username);
        String refreshToken = jwtServiceImpl.generateRefreshToken();

        try {
            redisOperator.opsForValue().set("refresh_token$" + refreshToken, username, Duration.ofDays(7));
        } catch (RuntimeException e) {
            throw new RuntimeException("Unable to save token on handleTokenProcessing() " + e.getMessage());
        }

        return List.of(
                ResponseCookie.from("jwt-token", jwtToken)
                        .path("/api/v1")
                        .httpOnly(true)
                        .secure(tokenCookieProperties.isSecure())
                        .sameSite(tokenCookieProperties.getSameSite())
                        .maxAge(Duration.ofMinutes(20))
                        .build(),
                ResponseCookie.from("refresh-token", refreshToken)
                        .path("/api/v1/")
                        .httpOnly(true)
                        .secure(tokenCookieProperties.isSecure())
                        .sameSite(tokenCookieProperties.getSameSite())
                        .maxAge(Duration.ofDays(7))
                        .build()
        );
    }

    @Transactional
    public void modifyUserPassword(PasswordResetRequestDTO passwordResetRequestDTO) {
        User registeredUser = userServiceImpl.checkAndGetUserByEmail(passwordResetRequestDTO.getEmail());

        if (registeredUser.getPasswordChangeOtp() == null ||
                !passwordEncoder.matches(passwordResetRequestDTO.getOtp(), registeredUser.getPasswordChangeOtp())) {
            throw new InvalidOtpException("Otp " + passwordResetRequestDTO.getOtp() + " is not valid");
        }

        if (registeredUser.getPasswordOtpExpireAt() < System.currentTimeMillis()) {
            throw new InvalidOtpException("Otp expired, please create a new one.");
        }

        registeredUser.setPassword(passwordEncoder.encode(passwordResetRequestDTO.getNewPassword()));
        registeredUser.setPasswordChangeOtp(null);
        registeredUser.setPasswordOtpExpireAt(null);

        userRepo.save(registeredUser);
    }

    @Transactional
    public void verifyUserEmail(AccountVerificationRequestDTO accountVerificationRequestDTO, String email) {
        User registeredUser = userServiceImpl.checkAndGetUserByEmail(email);

        if (registeredUser.getEmailVerifyOtp() == null ||
                !passwordEncoder.matches(accountVerificationRequestDTO.getOtp(), registeredUser.getEmailVerifyOtp())) {
            throw new InvalidOtpException("Otp " + accountVerificationRequestDTO.getOtp() + " is not valid");
        }

        if (registeredUser.getEmailVerifyOtpExpireAt() < System.currentTimeMillis()) {
            throw new InvalidOtpException("Otp expired, please create a new one.");
        }

        registeredUser.setAccountVerified(true);
        registeredUser.setEmailVerifyOtp(null);
        registeredUser.setEmailVerifyOtpExpireAt(null);

        userRepo.save(registeredUser);
    }

    private String extractToken(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh-token")) {
                    return cookie.getValue();
                }
            }
        }
        return "";
    }
}
