package com.project.hearmeout_backend.feed_service.dto.response;

import com.project.hearmeout_backend.authentication_service.model.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeUserProfileResponseDTO {
  @Schema(description = "The public username of the authenticated user")
  private String username;

  @Schema(
      description = "The unique identifier of the user, used for navigating to their full profile")
  private UUID userNavigationId;

  @Schema(description = "Indicates whether the user has verified their email account")
  private boolean accountVerified;

  @Schema(description = "List of roles assigned to the user", example = "[\"USER\"]")
  private RoleType role;
}
