package com.project.hearmeout_backend.user_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.hearmeout_backend.post_service.model.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAnswerResponseDTO {

  @Schema(description = "The detailed content of the user's answer")
  private String body;

  @Schema(description = "The current status of the answer (e.g., ACCEPTED)")
  private PostStatus postStatus;

  @Schema(description = "The net vote score of the answer")
  private int score;

  @Schema(description = "timestamp of when the answer was created")
  @JsonFormat(pattern = "dd-MM-yyy")
  private LocalDateTime updatedAt;

  @Schema(description = "The ID of the parent question post, used for navigation")
  private Long navigationPostId;

  @Schema(description = "The title of the parent question, providing context for the answer")
  private String parentPostTitle;
}
