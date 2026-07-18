package com.project.hearmeout_backend.user_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponseDTO {
  @Schema(description = "The unique identifier of the user")
  private UUID userId;

  @Schema(description = "The public username of the user")
  private String username;

  @Schema(description = "The full name of the user")
  private String fullName;

  @Schema(description = "The biography of the user")
  private String bio;

  @Schema(description = "The profession of the user")
  private String profession;

  @Schema(
      description =
          "The email address of the user (may be hidden or partial depending on privacy settings)")
  private String email;

  @Schema(
      description = "The user's current reputation score, earned through community contributions")
  private int reputation;

  @Schema(description = "timestamp of when the user account was created")
  @JsonFormat(pattern = "dd-MM-yyyy")
  private LocalDateTime createdAt;

  @Schema(
      description =
          "Indicates if the currently authenticated user has permission to modify this profile")
  private boolean isOperable;

  @Schema(description = "Indicates whether the user has verified their email address")
  private boolean isAccountVerified;

  @Schema(description = "Indicates whether the user's account has been terminated or deleted")
  private boolean isAccountTerminated;

  public UserProfileResponseDTO(
      UUID userId,
      String username,
      String email,
      int reputation,
      LocalDateTime createdAt,
      boolean isAccountVerified,
      boolean isAccountTerminated,
      String fullName,
      String bio,
      String profession) {
    this.userId = userId;
    this.username = username;
    this.email = email;
    this.reputation = reputation;
    this.createdAt = createdAt;
    this.isAccountVerified = isAccountVerified;
    this.isAccountTerminated = isAccountTerminated;
    this.fullName = fullName;
    this.bio = bio;
    this.profession = profession;
    this.isOperable = false;
  }
}
