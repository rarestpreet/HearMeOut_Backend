package com.project.hearmeout_backend.interaction_service.dto.request;

import com.project.hearmeout_backend.interaction_service.model.enums.CommentType;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentRequestDTO {

  @NotBlank(message = "Comment body is required")
  @Size(min = 20, max = 5000, message = "Comment must be between 20 and 5000 characters")
  @Schema(description = "The text content of the comment")
  private String body;

  @NotNull(message = "Parent ID is required")
  @Schema(description = "The ID of the parent entity (error report or solution)")
  private UUID parentId;

  @NotNull(message = "Parent type is required")
  @Schema(description = "The type of the parent entity (ERROR_REPORT or SOLUTION)")
  private PostType parentType;

  @NotNull(message = "Comment type is required")
  @Schema(description = "The type of comment (DISCUSSION, SUGGESTION, or DOUBT)")
  private CommentType commentType;
}
