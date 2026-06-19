package com.project.hearmeout_backend.authentication_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
public class AccountVerificationRequestDTO {

    @NotBlank(message = "Otp is required")
    @Length(min = 6, max = 6, message = "Provide valid 6 digit otp")
    @Schema(description = "The 6-digit OTP sent to the user's email for account verification")
    private String otp;
}
