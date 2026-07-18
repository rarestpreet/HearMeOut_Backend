package com.project.hearmeout_backend.post_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.hearmeout_backend.interaction_service.dto.response.CommentResponseDTO;
import com.project.hearmeout_backend.interaction_service.model.enums.VoteType;
import com.project.hearmeout_backend.post_service.model.enums.ErrorReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorReportResponseDTO {
  @Schema(description = "The unique identifier of the error report")
  private UUID id;

  @Schema(description = "The username of the author")
  private String authorUsername;

  @Schema(description = "The full name of the author")
  private String authorFullName;

  @Schema(description = "The title of the error report")
  private String title;

  @Schema(description = "Detailed description of the error")
  private String description;

  @Schema(description = "List of solutions")
  private List<SolutionResponseDTO> solutions;

  @Schema(description = "List of tags associated with the error report")
  private List<TagResponseDTO> tags;

  @Schema(description = "Whether the current user has voted on this error report")
  private boolean voted;

  @Schema(description = "The vote type cast by the current user")
  private VoteType voteType;

  @Schema(description = "List of comments on the error report")
  private List<CommentResponseDTO> comments;

  private boolean hasMoreSolutions;

  private boolean hasMoreComments;

  @Schema(description = "Timestamp of the last update")
  @JsonFormat(pattern = "dd-MM-yyyy")
  private LocalDateTime updatedAt;

  @Schema(description = "The current status of the error report")
  private ErrorReportStatus status;

  @Schema(description = "The net vote score")
  private int score;

  @Schema(description = "Whether the current user can edit or delete this error report")
  private boolean operable;

  private String language;
  private String languageVersion;
  private String framework;
  private String frameworkVersion;
  private String os;
  private String osVersion;
  private String errorType;
  private String reproductionSteps;
  private String repositoryUrl;
  private String branch;
  private String commitHash;
  private String filePath;
  private String relevantCode;
  private String relevantLog;

  // Constructor for JPA projection (fields available directly from DB query)
  public ErrorReportResponseDTO(
      UUID id,
      String authorUsername,
      String authorFullName,
      String title,
      String description,
      int score,
      LocalDateTime updatedAt,
      ErrorReportStatus status,
      String language,
      String languageVersion,
      String framework,
      String frameworkVersion,
      String os,
      String osVersion,
      String errorType,
      String reproductionSteps,
      String repositoryUrl,
      String branch,
      String commitHash,
      String filePath,
      String relevantCode,
      String relevantLog) {
    this.id = id;
    this.authorUsername = authorUsername;
    this.authorFullName = authorFullName;
    this.title = title;
    this.description = description;
    this.score = score;
    this.updatedAt = updatedAt;
    this.status = status;
    this.language = language;
    this.languageVersion = languageVersion;
    this.framework = framework;
    this.frameworkVersion = frameworkVersion;
    this.os = os;
    this.osVersion = osVersion;
    this.errorType = errorType;
    this.reproductionSteps = reproductionSteps;
    this.repositoryUrl = repositoryUrl;
    this.branch = branch;
    this.commitHash = commitHash;
    this.filePath = filePath;
    this.relevantCode = relevantCode;
    this.relevantLog = relevantLog;
  }
}
