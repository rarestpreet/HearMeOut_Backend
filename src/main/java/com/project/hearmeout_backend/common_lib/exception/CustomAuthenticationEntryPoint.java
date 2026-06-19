package com.project.hearmeout_backend.common_lib.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@NullMarked
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // improve filter chain exception handling
    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        response.getWriter().write("""
                {
                    "status": 401,
                    "error": "Unauthorized",
                    "message": "Invalid or missing token"
                }
                """);

        log.error("Unauthorized request handled: {}", authException.getMessage());
    }
}
