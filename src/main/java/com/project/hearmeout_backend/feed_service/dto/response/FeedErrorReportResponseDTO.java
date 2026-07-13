package com.project.hearmeout_backend.feed_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.hearmeout_backend.post_service.dto.response.ReportTagResponseDTO;
import com.project.hearmeout_backend.post_service.model.enums.ErrorReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedErrorReportResponseDTO {
  @Schema(description = "The unique identifier of the error report, used for navigation")
  private UUID navigationId;

  @Schema(description = "The username of the author")
  private String authorUsername;

  @Schema(description = "The title of the error report")
  private String title;

  @Schema(description = "The net vote score")
  private int score;

  @Schema(description = "Timestamp of when the post was last updated")
  @JsonFormat(pattern = "dd-MM-yyyy")
  private LocalDateTime updatedAt;

  @Schema(description = "The current status of the error report")
  private ErrorReportStatus status;

  @Schema(description = "List of tags associated with the error report")
  private List<ReportTagResponseDTO> tags;

  private int viewCount;

  // Constructor for JPA projection
  public FeedErrorReportResponseDTO(
      UUID navigationId,
      String authorUsername,
      String title,
      int score,
      LocalDateTime updatedAt,
      ErrorReportStatus status,
      int viewCount) {
    this.navigationId = navigationId;
    this.authorUsername = authorUsername;
    this.title = title;
    this.score = score;
    this.updatedAt = updatedAt;
    this.status = status;
    this.viewCount = viewCount;
  }
}
