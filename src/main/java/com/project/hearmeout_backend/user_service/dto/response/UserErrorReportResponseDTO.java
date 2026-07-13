package com.project.hearmeout_backend.user_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.hearmeout_backend.post_service.model.enums.ErrorReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserErrorReportResponseDTO {
  @Schema(description = "The unique identifier of the error report, used for navigation")
  private UUID navigationId;

  @Schema(description = "The title of the error report")
  private String title;

  @Schema(description = "The net vote score of the error report")
  private int score;

  @Schema(description = "Timestamp of when the error report was last modified")
  @JsonFormat(pattern = "dd-MM-yyyy")
  private LocalDateTime updatedAt;

  @Schema(description = "The current status of the error report")
  private ErrorReportStatus status;
}
