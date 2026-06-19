package com.project.hearmeout_backend.authentication_service.controller;

import com.project.hearmeout_backend.authentication_service.dto.request.AccountVerificationRequestDTO;
import com.project.hearmeout_backend.authentication_service.dto.request.LoginRequestDTO;
import com.project.hearmeout_backend.authentication_service.dto.request.PasswordResetRequestDTO;
import com.project.hearmeout_backend.authentication_service.dto.request.RegisterRequestDTO;
import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.authentication_service.service.implementation.SecurityServiceImpl;
import com.project.hearmeout_backend.common_lib.exception.EmailAlreadyExistException;
import com.project.hearmeout_backend.common_lib.exception.UserAlreadyExistException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Management", description = "Endpoints for user registration, login, logout, and account verification")
public class SecurityController {

    private final SecurityServiceImpl securityServiceImpl;

    @Operation(summary = "Register a new user account", description = "Registers a new user with the provided details. Fails if the username or email is already taken.")
    @PostMapping("register")
    public ResponseEntity<@NonNull String> registerUser(
            @Valid @RequestBody RegisterRequestDTO registerRequestDTO)
            throws UserAlreadyExistException, EmailAlreadyExistException {
        securityServiceImpl.createNewUser(registerRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @Operation(summary = "Logout user", description = "Logs out the currently authenticated user by invalidating their session cookie.")
    @PostMapping("logout")
    @PreAuthorize("isFullyAuthenticated()")
    public ResponseEntity<@NonNull String> logoutUser(
            @AuthenticationPrincipal CustomUserDetails currUser,
            HttpServletRequest request
    ) {
        List<ResponseCookie> clearedCookie = securityServiceImpl.terminateSession(request.getCookies(), currUser.getUserName());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, clearedCookie.get(0).toString(), clearedCookie.get(1).toString())
                .body("Session ended successfully");
    }

    @Operation(summary = "Login user", description = "Authenticates a user with email and password, and sets an HTTP-only session cookie upon success.")
    @PostMapping("login")
    public ResponseEntity<@NonNull String> loginUser(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        List<ResponseCookie> cookies = securityServiceImpl.authenticateUser(loginRequestDTO);

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookies.get(0).toString(), cookies.get(1).toString())
                .body("User logged in successfully");
    }

    @Operation(summary = "Reset user password", description = "Resets the user's password using a valid OTP and terminates their current session.")
    @PostMapping("password-reset")
    public ResponseEntity<@NonNull String> resetPassword(
            @Valid @RequestBody PasswordResetRequestDTO passwordResetRequestDTO,
            @AuthenticationPrincipal CustomUserDetails currUser,
            HttpServletRequest request
    ) {
        securityServiceImpl.modifyUserPassword(passwordResetRequestDTO);

        List<ResponseCookie> clearedCookie = securityServiceImpl.terminateSession(request.getCookies(), currUser.getUserName());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, clearedCookie.get(0).toString(), clearedCookie.get(1).toString())
                .body("Password updated successfully");
    }

    @Operation(summary = "Verify user account", description = "Verifies the user's account using a valid OTP sent to their email. Requires authentication.")
    @PostMapping("verify-account")
    @PreAuthorize("isFullyAuthenticated()")
    public ResponseEntity<@NonNull String> verifyAccount(
            @Valid @RequestBody AccountVerificationRequestDTO accountVerificationRequestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        securityServiceImpl.verifyUserEmail(accountVerificationRequestDTO, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.OK)
                .body("Account verified successfully");
    }

    @GetMapping("refresh-token")
    public ResponseEntity<@NonNull String> refreshToken(HttpServletRequest request) {
        log.info("number of cookies {}",request.getCookies().length);
        ResponseCookie cookie = securityServiceImpl
                .refreshAuthenticationTokens(request.getCookies());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Token refreshed successfully");
    }
}