package com.project.hearmeout_backend.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponseDTO {
  @Schema(description = "The HTTP status code of the error response")
  private int status;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  @Schema(description = "The exact timestamp when the exception occurred")
  private LocalDateTime timestamp;

  @Schema(description = "A brief description of the error type or reason")
  private String error;

  @Schema(description = "A detailed message explaining the error")
  private String message;

  @Schema(description = "A map containing field-specific validation errors, if applicable")
  private List<List<String>> fieldErrors;
}
