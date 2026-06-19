package com.project.hearmeout_backend.authentication_service.controller;

import com.project.hearmeout_backend.authentication_service.dto.request.PasswordResetOtpRequestDTO;
import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.authentication_service.service.implementation.EmailServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("mail")
@RequiredArgsConstructor
@Tag(name = "Email Management", description = "Endpoints for sending verification and password reset emails")
public class EmailController {

        private final EmailServiceImpl emailServiceImpl;

        @Operation(summary = "Send account verification OTP", description = "Sends an email with a One-Time Password (OTP) to verify the authenticated user's email address.")
        @PostMapping("email-verification-otp")
        @PreAuthorize("isFullyAuthenticated()")
        public ResponseEntity<@NonNull String> sendAccountVerificationOtp(
                        @AuthenticationPrincipal CustomUserDetails userDetails) {
                emailServiceImpl.sendAccountVerificationMail(userDetails.getUsername());

                return ResponseEntity.status(HttpStatus.OK)
                                .body("Account verification mail sent successfully");
        }

        @Operation(summary = "Send password reset OTP", description = "Sends an email with a One-Time Password (OTP) to allow a user to reset their password.")
        @PostMapping("reset-password-otp")
        public ResponseEntity<@NonNull String> sendResetPasswordOtp(
                        @Valid @RequestBody PasswordResetOtpRequestDTO passwordResetOtpRequestDTO) {
                emailServiceImpl.sendPasswordResetMail(passwordResetOtpRequestDTO.getEmail());

                return ResponseEntity.status(HttpStatus.OK)
                                .body("Account verification mail sent successfully");
        }
}
