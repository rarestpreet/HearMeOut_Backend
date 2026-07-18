package com.project.hearmeout_backend.post_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SolutionSubmitRequestDTO {

  @Schema(description = "The probable cause of the error")
  private String probableCause;

  @NotBlank(message = "Explanation is required")
  @Size(min = 50, max = 5000, message = "Explanation must be 50 to 5000 characters")
  @Schema(description = "Detailed explanation of the solution")
  private String explanation;

  @Schema(description = "Suggested code changes to fix the error")
  private String codeChange;

  @NotNull(message = "Language is required")
  @Schema(description = "Programming language")
  private String language;

  @Size(max = 10)
  @Schema(description = "Language version")
  private String languageVersion;

  @Schema(description = "Framework used")
  private String framework;

  @Size(max = 10)
  @Schema(description = "Framework version")
  private String frameworkVersion;

  @Schema(description = "Operating system")
  private String os;

  @Size(max = 10)
  @Schema(description = "OS version")
  private String osVersion;
}
