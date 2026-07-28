package com.project.hearmeout_backend.common_lib.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.hearmeout_backend.gateway.dto.response.ExceptionResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

@NullMarked
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  // improve filter chain exception handling
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    ExceptionResponseDTO exceptionResponse =
        ExceptionResponseDTO.builder()
            .status(401)
            .error("Internal authentication service exception")
            .message(authException.getMessage())
            .timestamp(LocalDateTime.now())
            .build();

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");

    response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));

    log.error("Unauthorized request handled: {}", authException.getMessage());
  }
}
