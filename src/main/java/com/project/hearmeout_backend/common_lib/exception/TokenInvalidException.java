package com.project.hearmeout_backend.common_lib.exception;

public class TokenInvalidException extends RuntimeException {
  public TokenInvalidException(String message) {
    super(message);
  }
}
