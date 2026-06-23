package com.project.hearmeout_backend.common_lib.exception;

public class InvalidOtpException extends RuntimeException {
  public InvalidOtpException(String message) {
    super(message);
  }
}
