package com.project.hearmeout_backend.authentication_service.model;

import com.project.hearmeout_backend.user_service.dto.response.UserDetailResponseDTO;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
@NullMarked
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

  private final UserDetailResponseDTO user;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.name())).toList();
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  public String getUsername() {
    return user.getEmail();
  }

  public Long getUserId() {
    return user.getUserId();
  }

  public String getName() {
    return user.getUsername();
  }
}
