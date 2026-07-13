package com.project.hearmeout_backend.user_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.hearmeout_backend.post_service.model.enums.SolutionStatus;
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
public class UserAnswerResponseDTO {

  @Schema(description = "The explanation of the user's solution")
  private String explanation;

  @Schema(description = "The current status of the solution")
  private SolutionStatus status;

  @Schema(description = "The net vote score of the solution")
  private int score;

  @Schema(description = "Timestamp of when the solution was last updated")
  @JsonFormat(pattern = "dd-MM-yyyy")
  private LocalDateTime updatedAt;

  @Schema(description = "The ID of the parent error report, used for navigation")
  private UUID navigationId;

  @Schema(description = "The title of the parent error report")
  private String parentReportTitle;
}
