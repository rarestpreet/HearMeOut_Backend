package com.project.hearmeout_backend.post_service.dto.request;

import com.project.hearmeout_backend.post_service.model.enums.Framework;
import com.project.hearmeout_backend.post_service.model.enums.OperatingSystem;
import com.project.hearmeout_backend.post_service.model.enums.ProgrammingLanguage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ErrorReportSubmitRequestDTO {

  @NotBlank(message = "Title is required")
  @Size(min = 15, max = 100, message = "Title must be 15 to 100 characters long")
  @Schema(description = "A concise summary of the error being reported")
  private String title;

  @NotBlank(message = "Description is required")
  @Size(min = 50, max = 5000, message = "Description must be 50 to 5000 characters")
  @Schema(description = "Detailed description of the error, providing context and specifics")
  private String description;

  @Schema(description = "Steps to reproduce the error")
  private String reproductionSteps;

  @NotBlank(message = "Error type is required")
  @Size(max = 50, message = "Error type must be at most 50 characters")
  @Schema(description = "The type/category of the error (e.g., NullPointerException, TypeError)")
  private String errorType;

  @Size(max = 200)
  @Schema(description = "URL of the repository containing the code")
  private String repositoryUrl;

  @Size(max = 100)
  @Schema(description = "Branch name where the error occurs")
  private String branch;

  @Size(max = 100)
  @Schema(description = "Commit hash where the error occurs")
  private String commitHash;

  @Size(max = 200)
  @Schema(description = "File path where the error occurs")
  private String filePath;

  @Schema(description = "The relevant code snippet causing the error")
  private String relevantCode;

  @Schema(description = "Relevant log output or stack trace")
  private String relevantLog;

  @NotNull(message = "Language is required")
  @Schema(description = "Programming language (e.g., Java, Python)")
  private ProgrammingLanguage language;

  @Size(max = 10)
  @Schema(description = "Language version (e.g., 17, 3.11)")
  private String languageVersion;

  @Schema(description = "Framework used (e.g., Spring Boot, Django)")
  private Framework framework;

  @Size(max = 10)
  @Schema(description = "Framework version")
  private String frameworkVersion;

  @Schema(description = "Operating system (e.g., Windows, Linux)")
  private OperatingSystem os;

  @Size(max = 10)
  @Schema(description = "OS version")
  private String osVersion;

  @NotEmpty(message = "At least one tag is required")
  @Size(min = 1, max = 5, message = "Must contain 1 to 5 tags")
  @Schema(description = "A list of tag IDs categorizing the error report")
  private List<UUID> tagIds;
}
