package com.project.hearmeout_backend.authentication_service.controller;

import com.project.hearmeout_backend.authentication_service.dto.request.PasswordResetOtpRequestDTO;
import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.common_lib.event_dto.PasswordResetOtpEvent;
import com.project.hearmeout_backend.common_lib.event_dto.VerificationOtpEvent;
import com.project.hearmeout_backend.gateway.annotation.RateLimiter;
import com.project.hearmeout_backend.gateway.model.enums.RateLimits;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("mail")
@RequiredArgsConstructor
@Tag(
    name = "Email Management",
    description = "Endpoints for sending verification and password reset emails")
public class EmailController {

  private final RabbitTemplate rabbitTemplate;

  @Operation(
      summary = "Send account verification OTP",
      description =
          """
          Sends an email with a One-Time Password (OTP) to verify the authenticated user's email address.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: USER
          - Denied Roles: VERIFIED_USER, ADMIN, GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PostMapping("email-verification-otp")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('USER')")
  @RateLimiter(
      limitType = RateLimits.EMAIL_VERIFICATION_OTP,
      requestAllowed = 1,
      timeoutInMinutes = 1)
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<@NonNull String> sendAccountVerificationOtp(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    rabbitTemplate.convertAndSend(
        "email.exchange",
        "email.verification-otp",
        new VerificationOtpEvent(userDetails.getUsername()));

    return ResponseEntity.status(HttpStatus.OK).body("Account verification mail sent successfully");
  }

  @Operation(
      summary = "Send password reset OTP",
      description =
          """
          Sends an email with a One-Time Password (OTP) to allow a user to reset their password.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: USER, VERIFIED_USER
          - Denied Roles: ADMIN, GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PostMapping("reset-password-otp")
  @RateLimiter(limitType = RateLimits.PASSWORD_RESET_OTP, requestAllowed = 1, timeoutInMinutes = 1)
  @PreAuthorize("!hasAuthority('ADMIN')")
  public ResponseEntity<@NonNull String> sendResetPasswordOtp(
      @Valid @RequestBody PasswordResetOtpRequestDTO passwordResetOtpRequestDTO) {
    rabbitTemplate.convertAndSend(
        "email.exchange",
        "email.password-reset",
        new PasswordResetOtpEvent(passwordResetOtpRequestDTO.getEmail()));

    return ResponseEntity.status(HttpStatus.OK).body("Account verification mail sent successfully");
  }
}
