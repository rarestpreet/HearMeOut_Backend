package com.project.hearmeout_backend.administration_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminTagResponseDTO {
  @Schema(description = "The unique identifier of the tag")
  private UUID tagId;

  @Schema(description = "The concise name of the tag")
  private String name;

  @Schema(description = "A brief description explaining the tag's purpose")
  private String description;

  @Schema(description = "Count of error reports that used this tag")
  private int usageCount;

  @Schema(description = "Indicates whether the tag is approved by an admin")
  private boolean isApproved;
}
