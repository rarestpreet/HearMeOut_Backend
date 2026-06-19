package com.project.hearmeout_backend.authentication_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordResetOtpRequestDTO {

    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Email must be valid"
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Schema(description = "The registered email address to send the password reset OTP to")
    private String email;
}
