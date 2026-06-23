package com.project.hearmeout_backend.common_lib.exception;

public class RateLimitExceededException extends Throwable {
  public RateLimitExceededException(String message) {
    super(message);
  }
}
