package com.project.hearmeout_backend.gateway.filter;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.authentication_service.service.implementation.CustomUserDetailsServiceImpl;
import com.project.hearmeout_backend.authentication_service.service.implementation.JwtServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@NullMarked
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final JwtServiceImpl jwtService;
  private final CustomUserDetailsServiceImpl customUserDetailsServiceImpl;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // avoid authentication when not needed
    if (request.getRequestURI().equals("/api/v1/auth/login")
        || request.getRequestURI().equals("/api/v1/auth/register")) {
      filterChain.doFilter(request, response);

      return;
    }

    String token = null;

    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (cookie.getName().equals("jwt-token")) {
          token = cookie.getValue();
          break;
        }
      }
    }

    if (token == null) {
      filterChain.doFilter(request, response);

      return;
    }
    try {
      String username = jwtService.extractUsername(token);

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        CustomUserDetails userDetails = customUserDetailsServiceImpl.loadUserByUsername(username);

        boolean isTokenValid = jwtService.isTokenValid(token, userDetails);

        if (isTokenValid) {
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());

          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (Exception e) {
      SecurityContextHolder.clearContext();
      log.warn("Failed to validate JWT token: {}", e.getMessage());
    }
    filterChain.doFilter(request, response);
  }
}
