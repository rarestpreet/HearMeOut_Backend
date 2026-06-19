package com.project.hearmeout_backend.gateway.aspect;

import com.project.hearmeout_backend.gateway.annotation.RateLimiter;
import com.project.hearmeout_backend.gateway.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Component
@Aspect
public class RateLimiterAspect {

    private final int refillRate;
    private final int maxCapacity;
    private long lastRefillTime;
    private int tokens;

    public RateLimiterAspect() {
        refillRate = 2;
        maxCapacity = 1000;
        tokens = 0;
        lastRefillTime = System.currentTimeMillis();
    }

    @Around("@annotation(rateLimiter)")
    public Object allowRequest(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        refill();
        System.out.println(rateLimiter.value());
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        Objects.requireNonNull(attributes, "Invalid input: ServletRequestAttributes ");
        HttpServletRequest request = attributes.getRequest();

        if(tokens > 0){
            tokens--;
            return joinPoint.proceed();
        }
        else {
            throw new RateLimitExceededException(request.getRequestURL().toString());
        }
    }

    public void refill() {
        int refillAmount = (int) (System.currentTimeMillis() - lastRefillTime) * refillRate;
        tokens = Math.min(refillAmount, maxCapacity);
        lastRefillTime = System.currentTimeMillis();
    }
}
