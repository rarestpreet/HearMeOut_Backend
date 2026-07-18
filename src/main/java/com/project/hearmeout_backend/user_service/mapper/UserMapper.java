package com.project.hearmeout_backend.user_service.mapper;

import com.project.hearmeout_backend.authentication_service.dto.request.RegisterRequestDTO;
import com.project.hearmeout_backend.authentication_service.model.enums.RoleType;
import com.project.hearmeout_backend.common_lib.utils.StringFormatter;
import com.project.hearmeout_backend.user_service.model.User;

public class UserMapper {

  public static User toProfileEntity(RegisterRequestDTO registerDTO, String encryptedPassword) {
    return User.builder()
        .username(registerDTO.getUsername())
        .password(encryptedPassword)
        .email(registerDTO.getEmail())
        .fullName(StringFormatter.capitalizeWords(registerDTO.getFullName()))
        .bio(registerDTO.getBio())
        .role(RoleType.USER)
        .build();
  }
}
