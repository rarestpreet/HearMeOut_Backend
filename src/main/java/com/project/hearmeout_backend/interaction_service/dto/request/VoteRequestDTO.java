package com.project.hearmeout_backend.interaction_service.dto.request;

import com.project.hearmeout_backend.post_service.model.enums.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VoteRequestDTO {

  @NotNull(message = "Parent ID cannot be null")
  @Schema(description = "The ID of the entity (error report or solution) being voted on")
  private UUID parentId;

  @NotNull(message = "Parent type cannot be null")
  @Schema(description = "The type of entity being voted on (ERROR_REPORT or SOLUTION)")
  private PostType parentType;
}
