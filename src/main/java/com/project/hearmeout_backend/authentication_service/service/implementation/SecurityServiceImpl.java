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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        if (userRepo
                .existsByUsernameOrEmail(registerRequestDTO.getUsername(), registerRequestDTO.getEmail())
        ) {
            throw new UserAlreadyExistException(
                    "User with similar username or email already exist"
            );
        }

        User user = UserMapper
                .toProfileEntity(
                        registerRequestDTO,
                        passwordEncoder
                                .encode(registerRequestDTO.getPassword())
                );

        userRepo
                .save(user);
        log.info(
                "Successfully created new user account for email: {}",
                registerRequestDTO.getEmail()
        );

        emailServiceImpl
                .sendWelcomeMail(
                        registerRequestDTO.getEmail(),
                        registerRequestDTO.getUsername()
                );
    }

    public List<ResponseCookie> terminateSession(String email) {
        HttpSession session = httpServletRequest
                .getSession(false);
        if (session != null) {
            session
                    .invalidate();
        }
        SecurityContextHolder.getContext()
                .setAuthentication(null);

        try {
            String[] refreshTokens = Objects
                    .requireNonNull(
                            redisOperator.opsForValue()
                                    .get("user_session$".concat(email))
                    ).split("\\$");

            Arrays.stream(refreshTokens)
                    .forEach(token -> {
                        redisOperator
                                .delete("refresh_token$" + token);
                    });
        } catch (RuntimeException e) {
            throw new RuntimeException(
                    "Unable to delete token on terminateSession() " + e.getMessage()
            );
        }
        log.info(
                "Terminated user session for {}",
                email
        );

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

    public List<ResponseCookie> authenticateUser(LoginRequestDTO request) {
        int loginRequestCount = redisOperator.opsForValue()
                .get(
                        "login_request_count$"
                                .concat(request.getEmail())
                ) == null ?
                0 :
                Integer.parseInt(
                        Objects.requireNonNull(
                                redisOperator.opsForValue()
                                        .get(
                                                "login_request_count$"
                                                        .concat(request.getEmail())
                                        )
                        )
                );

        try {
            authManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getEmail(), request.getPassword()
                            )
                    );
            redisOperator
                    .delete(
                            "login_request_count$"
                                    .concat(request.getEmail())
                    );
        } catch (AuthenticationException e) {
            log.warn(
                    "Login attempt failed for {}, total attempts = {}: {}",
                    request.getEmail(),
                    loginRequestCount,
                    e.getMessage()
            );

            redisOperator.opsForValue()
                    .set(
                            "login_request_count$"
                                    .concat(request.getEmail()),
                            String
                                    .valueOf(loginRequestCount + 1),
                            Duration
                                    .ofHours(1)
                    );

            throw new BadCredentialsException(
                    "Login attempt failed, please enter valid email and password"
            );
        }

        List<ResponseCookie> tokens = handleTokenProcessing(request.getEmail());

        String currentTokens = redisOperator.opsForValue()
                .get(
                        "user_session$"
                                .concat(request.getEmail())
                );
        redisOperator
                .delete(
                        "login_request_count$"
                                .concat(request.getEmail())
                );
        redisOperator.opsForValue()
                .set(
                        "user_session$"
                                .concat(request.getEmail()),
                        currentTokens == null ?
                                tokens.get(1)
                                        .getValue() :
                                currentTokens
                                        .concat(
                                                "$".concat(tokens.get(1).getValue())
                                        ),
                        Duration
                                .ofHours(1)
                );

        return tokens;
    }

    public ResponseCookie refreshAuthenticationTokens(Cookie[] cookies) {
        String refreshToken = extractToken(cookies);

        if (!refreshToken.isBlank()) {
            try {
                String username = redisOperator.opsForValue()
                        .get(
                                "refresh_token$" + refreshToken
                        );
                CustomUserDetails currUser = customUserDetailsServiceImpl
                        .loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                currUser,
                                null,
                                currUser.getAuthorities()
                        );
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);

                String jwtToken = jwtServiceImpl
                        .generateJwtToken(username);

                return ResponseCookie.from("jwt-token", jwtToken)
                        .path("/api/v1")
                        .httpOnly(true)
                        .secure(tokenCookieProperties.isSecure())
                        .sameSite(tokenCookieProperties.getSameSite())
                        .maxAge(Duration.ofMinutes(20))
                        .build();
            } catch (RuntimeException e) {
                throw new RuntimeException(
                        "Unable to refresh authentication token on refreshAuthenticationToken() " + e.getMessage()
                );
            }
        } else {
            throw new TokenInvalidException(
                    "Authentication token is invalid, token refresh failed"
            );
        }
    }

    public List<ResponseCookie> handleTokenProcessing(String username) {
        String jwtToken = jwtServiceImpl
                .generateJwtToken(username);
        String refreshToken = jwtServiceImpl
                .generateRefreshToken();

        try {
            redisOperator.opsForValue()
                    .set(
                            "refresh_token$" + refreshToken,
                            username,
                            Duration
                                    .ofDays(7)
                    );
        } catch (RuntimeException e) {
            throw new RuntimeException(
                    "Unable to save token on handleTokenProcessing() " + e.getMessage()
            );
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
        User registeredUser = userServiceImpl
                .checkAndGetUserByEmail(passwordResetRequestDTO.getEmail());
        String storedOtp = redisOperator.opsForValue()
                .getAndDelete(
                        "passresetotp$"
                                .concat(passwordResetRequestDTO.getEmail())
                );

        if (storedOtp == null) {
            throw new InvalidOtpException(
                    "Otp expired for password reset, please create a new one."
            );
        }

        if (!passwordEncoder
                .matches(passwordResetRequestDTO.getOtp(), storedOtp)
        ) {
            throw new InvalidOtpException(
                    "Otp for password reset " + passwordResetRequestDTO.getOtp() + " is not valid"
            );
        }

        registeredUser
                .setPassword(
                        passwordEncoder
                                .encode(passwordResetRequestDTO.getNewPassword())
                );
        userRepo
                .save(registeredUser);
    }

    @Transactional
    public void verifyUserEmail(AccountVerificationRequestDTO accountVerificationRequestDTO, String email) {
        User registeredUser = userServiceImpl
                .checkAndGetUserByEmail(email);
        String storedOtp = redisOperator.opsForValue()
                .getAndDelete(
                        "emailverifyotp$"
                                .concat(email)
                );

        if (storedOtp == null) {
            throw new InvalidOtpException(
                    "Otp expired for account verification, please create a new one."
            );
        }

        if (!passwordEncoder
                .matches(
                        accountVerificationRequestDTO.getOtp(),
                        storedOtp
                )
        ) {
            throw new InvalidOtpException(
                    "Otp for account verification " + accountVerificationRequestDTO.getOtp() + " is not valid"
            );
        }

        registeredUser
                .setAccountVerified(true);
        userRepo
                .save(registeredUser);
    }

    private String extractToken(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName()
                        .equals("refresh-token")
                ) {
                    return cookie.getValue();
                }
            }
        }
        return "";
    }
}
