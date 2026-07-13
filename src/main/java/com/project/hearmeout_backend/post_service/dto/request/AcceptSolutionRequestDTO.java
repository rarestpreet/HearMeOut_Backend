package com.project.hearmeout_backend.post_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AcceptSolutionRequestDTO {
  @Schema(description = "The unique identifier of the error report")
  private UUID errorReportId;

  @Schema(description = "The unique identifier of the solution being accepted")
  private UUID solutionId;
}
