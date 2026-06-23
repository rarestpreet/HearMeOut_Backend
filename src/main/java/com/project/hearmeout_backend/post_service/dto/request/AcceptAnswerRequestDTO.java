package com.project.hearmeout_backend.post_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AcceptAnswerRequestDTO {
  @Schema(description = "The unique identifier of the question")
  private Long questionId;

  @Schema(description = "The unique identifier of the answer being accepted")
  private Long answerId;
}
