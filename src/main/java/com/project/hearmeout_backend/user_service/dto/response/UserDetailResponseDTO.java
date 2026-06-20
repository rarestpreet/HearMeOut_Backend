package com.project.hearmeout_backend.user_service.dto.response;

import com.project.hearmeout_backend.authentication_service.model.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserDetailResponseDTO {
    private final Long userId;
    private final String username;
    private final String email;
    private final String password;
    private final List<RoleType> roles;

    @Override
    public String toString() {
        return userId + " " + username + " " + email + " " + password + " " + roles;
    }
}
