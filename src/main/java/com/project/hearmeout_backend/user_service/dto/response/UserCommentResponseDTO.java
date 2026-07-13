package com.project.hearmeout_backend.user_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCommentResponseDTO {

  @Schema(description = "The text content of the user's comment")
  private String body;

  @Schema(description = "The ID of the parent entity, used for navigation")
  private UUID parentId;

  @Schema(description = "The type of the parent entity")
  private PostType parentType;

  @Schema(description = "Timestamp of the last update")
  @JsonFormat(pattern = "dd-MM-yyyy")
  private LocalDateTime updatedAt;
}
