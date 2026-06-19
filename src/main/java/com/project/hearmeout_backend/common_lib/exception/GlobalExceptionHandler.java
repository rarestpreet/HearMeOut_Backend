package com.project.hearmeout_backend.common_lib.exception;

import com.project.hearmeout_backend.gateway.dto.response.ExceptionResponseDTO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<@NonNull ExceptionResponseDTO> handleInternalAuthenticationServiceException(
            InternalAuthenticationServiceException ex) {
        log.error("Internal authentication failed: {}", ex.getLocalizedMessage());
        assert ex.getAuthenticationRequest() != null;
        ExceptionResponseDTO response = ExceptionResponseDTO.builder()
                .status(400)
                .error("Internal authentication service exception")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<@NonNull ExceptionResponseDTO> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        ExceptionResponseDTO response = ExceptionResponseDTO.builder()
                .status(404)
                .error("User not found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<@NonNull ExceptionResponseDTO> handleEmailAlreadyExistException(
            EmailAlreadyExistException ex) {
        log.warn("Email already exist: {}", ex.getMessage());
        ExceptionResponseDTO response = ExceptionResponseDTO.builder()
                .status(409)
                .error("Email already exist")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<@NonNull ExceptionResponseDTO> handlePostNotFoundException(PostNotFoundException ex) {
        log.warn("Post not found: {}", ex.getMessage());
        ExceptionResponseDTO response = ExceptionResponseDTO.builder()
                .status(404)
                .error("Post not found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<@NonNull ExceptionResponseDTO> handleCommentNotFoundException(
            CommentNotFoundException ex) {
        log.warn("Comment not found: {}", ex.getMessage());
        ExceptionResponseDTO response = ExceptionResponseDTO.builder()
                .status(404)
                .error("Comment not found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<@NonNull ExceptionResponseDTO> handleUserAlreadyExistException(
            UserAlreadyExistException ex) {
        log.warn("Username already exist: {}", ex.getMessage());
        ExceptionResponseDTO response = ExceptionResponseDTO.builder()
                .status(409)
                .error("Username already exist")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull ExceptionResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        ExceptionResponseDTO response = ExceptionResponseDTO.builder()
                .status(400)
                .error("Validation failed")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(TagNotFoundException.class)
    public ResponseEntity<@NonNull ExceptionResponseDTO> handleTagNotFoundException(TagNotFoundException ex) {
        log.warn("Tag not found: {}", ex.getMessage());
        ExceptionResponseDTO response = ExceptionResponseDTO.builder()
                .status(404)
                .error("Tag not found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<@NonNull ExceptionResponseDTO> handleInvalidOperationException(
            InvalidOperationException ex) {
        log.warn("Invalid operation requested: {}", ex.getMessage());
        ExceptionResponseDTO response = ExceptionResponseDTO.builder()
                .status(403)
                .error("Invalid operation")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<@NonNull ExceptionResponseDTO> handleInvalidOtpException(
            InvalidOperationException ex) {
        log.warn("Invalid otp received: {}", ex.getMessage());
        ExceptionResponseDTO response = ExceptionResponseDTO.builder()
                .status(400)
                .error("Invalid otp")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<@NonNull ExceptionResponseDTO> handleRateLimitExceededException(
            RateLimitExceededException ex) {
        log.warn("Rate limit for request {} exceeded", ex.getMessage());
        ExceptionResponseDTO response = ExceptionResponseDTO.builder()
                .status(429)
                .error("Rate limit exceeded")
                .message("Rate limit for request " + ex.getMessage() + " exceeded")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<@NonNull ExceptionResponseDTO> handleTokenInvalidException(
            TokenInvalidException ex) {
        log.warn("Authentication token is invalid");
        ExceptionResponseDTO response = ExceptionResponseDTO.builder()
                .status(401)
                .error("Token invalid")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<@NonNull ExceptionResponseDTO> handleRuntimeException(
            RuntimeException ex) {
        log.warn("Error on runtime exception: {}", ex.getMessage());
        ExceptionResponseDTO response = ExceptionResponseDTO.builder()
                .status(500)
                .error("Runtime exception")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}