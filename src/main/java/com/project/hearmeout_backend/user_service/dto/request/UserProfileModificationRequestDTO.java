package com.project.hearmeout_backend.user_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileModificationRequestDTO {

    @NotBlank(message = "Username is required")
    @Length(max = 20, message = "Username must be less than 20 characters")
    @Schema(description = "The new username to set for the user profile")
    private String username;

    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Email must be valid"
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Schema(description = "The new email address to set for the user profile")
    private String email;
}
