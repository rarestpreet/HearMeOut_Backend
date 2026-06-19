package com.project.hearmeout_backend.gateway.exception;

public class RateLimitExceededException extends Throwable {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
