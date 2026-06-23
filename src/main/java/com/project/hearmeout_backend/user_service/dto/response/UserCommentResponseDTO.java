package com.project.hearmeout_backend.user_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCommentResponseDTO {

  @Schema(description = "The text content of the user's comment")
  private String body;

  @Schema(description = "The ID of the parent post, used for navigation")
  private Long navigationPostId;

  @Schema(description = "A snippet or glimpse of the content of the associated post")
  private String postContent;

  @Schema(description = "timestamp of the last update")
  @JsonFormat(pattern = "dd-MM-yyyy")
  private LocalDateTime updatedAt;
}
