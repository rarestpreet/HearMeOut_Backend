package com.project.hearmeout_backend.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@NullMarked
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    long startTime = System.currentTimeMillis();

    try {
      MDC.put("requestId", UUID.randomUUID().toString());
      MDC.put("requestMethod", request.getMethod());
      MDC.put("requestURI", request.getRequestURI());

      log.info("Incoming request");

      filterChain.doFilter(request, response);

    } catch (Exception e) {
      log.warn("Exception occurred: \n", e);
    } finally {
      long duration = System.currentTimeMillis() - startTime;

      if (response.getStatus() >= 400) {
        log.info("Completed with error status {} in {} ms", response.getStatus(), duration);
      } else log.info("Completed with status {} in {} ms", response.getStatus(), duration);

      MDC.clear();
    }
  }
}
