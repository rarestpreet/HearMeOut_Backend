package com.project.hearmeout_backend.interaction_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponseDTO {
  @Schema(description = "The unique identifier of the comment")
  private UUID commentId;

  @Schema(description = "The text content of the comment")
  private String body;

  @Schema(description = "The username of the comment's author")
  private String authorUsername;

  @Schema(description = "The ID of the parent entity for navigation purposes")
  private UUID parentId;

  @Schema(description = "Timestamp of the last update")
  @JsonFormat(pattern = "dd-MM-yyyy")
  private LocalDateTime updatedAt;

  @Schema(description = "Whether the current user can edit or delete this comment")
  private boolean operable;

  // Constructor for JPA projection
  public CommentResponseDTO(
      UUID commentId, String body, String authorUsername, UUID parentId, LocalDateTime updatedAt) {
    this.commentId = commentId;
    this.body = body;
    this.authorUsername = authorUsername;
    this.parentId = parentId;
    this.updatedAt = updatedAt;
  }
}
