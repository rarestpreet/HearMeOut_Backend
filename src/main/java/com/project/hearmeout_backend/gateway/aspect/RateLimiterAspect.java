package com.project.hearmeout_backend.gateway.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.hearmeout_backend.authentication_service.dto.request.PasswordResetOtpRequestDTO;
import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.gateway.annotation.RateLimiter;
import com.project.hearmeout_backend.gateway.model.enums.RateLimits;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class RateLimiterAspect {

    /* Implement token bucket algo

    private final int refillRate;
    private final int maxCapacity;
    private long lastRefillTime;

    public RateLimiterAspect() {
        refillRate = 2;
        maxCapacity = 1000;
        lastRefillTime = System.currentTimeMillis();
    }
     */

    private final StringRedisTemplate redisOperator;

    @Before("@annotation(rateLimiter)")
    public void allowRequest(JoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {

        switch (rateLimiter.limitType()) {
            case RateLimits.EMAIL_VERIFICATION_OTP -> validateEmailVerificationOtpRequest(joinPoint.getArgs());
            case RateLimits.PASSWORD_RESET_OTP -> validatePasswordResetOtpRequest(joinPoint.getArgs());
        }
    }

    public void refill() {
        //TODO
    }

    private void validateEmailVerificationOtpRequest(Object[] args) {
        CustomUserDetails currUser = (CustomUserDetails) args[0];

        if (redisOperator.opsForValue()
                .get("emailverifycooldown$".concat(currUser.getUsername())) != null) {
            throw new InvalidOperationException("Please wait few seconds before requesting new otp for email verification");
        }
    }

    private void validatePasswordResetOtpRequest(Object[] args) {
        PasswordResetOtpRequestDTO currUser = (PasswordResetOtpRequestDTO) args[0];

        if (redisOperator.opsForValue()
                .get("passresetcooldown$".concat(currUser.getEmail())) != null) {
            throw new InvalidOperationException("Please wait few seconds before requesting new otp for password reset");
        }
    }
}
