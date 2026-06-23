package com.project.hearmeout_backend.authentication_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
public class PasswordResetRequestDTO {

  @NotBlank(message = "Password is required")
  @Length(min = 8, max = 20, message = "Password must be within 8 and 20 character long")
  @Schema(description = "The new password to set for the account")
  private String newPassword;

  @Pattern(
      regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
      message = "Email must be valid")
  @NotBlank(message = "Email is required")
  @Email(message = "Email must be a valid email address")
  @Schema(description = "The registered user's email address")
  private String email;

  @NotBlank(message = "Otp is required")
  @Length(min = 6, max = 6, message = "Provide valid 6 digit otp")
  @Schema(description = "The 6-digit OTP sent to the user's email to verify the password reset")
  private String otp;
}
