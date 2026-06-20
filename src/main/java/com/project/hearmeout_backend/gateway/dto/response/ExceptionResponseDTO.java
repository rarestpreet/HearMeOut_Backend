package com.project.hearmeout_backend.gateway.dto.response;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class ExceptionResponseDTO {
    @Schema(description = "The HTTP status code of the error response")
    private int status;

    @Schema(description = "The exact timestamp when the exception occurred")
    private LocalDateTime timestamp;

    @Schema(description = "A brief description of the error type or reason")
    private String error;

    @Schema(description = "A detailed message explaining the error")
    private String message;

    @Schema(description = "A map containing field-specific validation errors, if applicable")
    private List<List<String>> fieldErrors;
}
