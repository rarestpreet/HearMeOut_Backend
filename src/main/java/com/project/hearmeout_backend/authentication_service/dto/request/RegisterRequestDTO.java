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
public class RegisterRequestDTO {

  @NotBlank(message = "Username is required")
  @Length(min = 5, max = 20, message = "Username must be less than 20 character")
  @Schema(description = "The unique username chosen by the user")
  private String username;

  @NotBlank(message = "Password is required")
  @Length(min = 8, max = 20, message = "Password must be within 8 and 20 character long")
  @Schema(description = "The password for the new account")
  private String password;

  @Pattern(
      regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
      message = "Email must be valid")
  @NotBlank(message = "Email is required")
  @Email(message = "Email must be a valid email address")
  @Schema(description = "The unique email address for the user")
  private String email;
}
