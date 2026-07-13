package com.project.hearmeout_backend.user_service.dto.response;

import com.project.hearmeout_backend.authentication_service.model.enums.RoleType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDetailResponseDTO {
  private final UUID userId;
  private final String username;
  private final String email;
  private final String password;
  private final RoleType role;

  @Override
  public String toString() {
    return userId + " " + username + " " + email + " " + password + " " + role;
  }
}
