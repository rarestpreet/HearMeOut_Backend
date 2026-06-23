package com.project.hearmeout_backend.post_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TagResponseDTO {
  @Schema(description = "The unique identifier of the tag")
  private Long tagId;

  @Schema(description = "The concise name of the tag")
  private String name;

  @Schema(description = "A brief description explaining the tag's purpose")
  private String description;
}
