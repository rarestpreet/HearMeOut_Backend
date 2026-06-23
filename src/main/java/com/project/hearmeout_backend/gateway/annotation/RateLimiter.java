package com.project.hearmeout_backend.gateway.annotation;

import com.project.hearmeout_backend.gateway.model.enums.RateLimits;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RateLimiter {
  int requestAllowed();

  RateLimits limitType();

  long timeoutInMinutes();

  long timeToRefreshTokenInMinutes() default 0;
}
