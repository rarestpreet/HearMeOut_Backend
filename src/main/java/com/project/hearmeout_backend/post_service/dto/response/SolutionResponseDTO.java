package com.project.hearmeout_backend.post_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.hearmeout_backend.interaction_service.dto.response.CommentResponseDTO;
import com.project.hearmeout_backend.interaction_service.model.enums.VoteType;
import com.project.hearmeout_backend.post_service.model.enums.Framework;
import com.project.hearmeout_backend.post_service.model.enums.OperatingSystem;
import com.project.hearmeout_backend.post_service.model.enums.ProgrammingLanguage;
import com.project.hearmeout_backend.post_service.model.enums.SolutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SolutionResponseDTO {
  @Schema(description = "The unique identifier of the solution")
  private UUID id;

  @Schema(description = "The username of the solution's author")
  private String authorUsername;

  @Schema(description = "The explanation of the solution")
  private String explanation;

  @Schema(description = "Whether the current user has voted on this solution")
  private boolean voted;

  @Schema(description = "The vote type cast by the current user")
  private VoteType voteType;

  @Schema(description = "Timestamp of the last update")
  @JsonFormat(pattern = "dd-MM-yyyy")
  private LocalDateTime updatedAt;

  @Schema(description = "The current status of the solution")
  private SolutionStatus status;

  @Schema(description = "List of comments on the solution")
  private List<CommentResponseDTO> comments;

  private boolean hasMoreComments;

  @Schema(description = "The net vote score")
  private int score;

  @Schema(description = "Whether the current user can edit or delete this solution")
  private boolean operable;

  private ProgrammingLanguage language;
  private String languageVersion;
  private Framework framework;
  private String frameworkVersion;
  private OperatingSystem os;
  private String osVersion;
  private String probableCause;
  private String codeChange;

  // Constructor for JPA projection
  public SolutionResponseDTO(
      UUID id,
      String authorUsername,
      String explanation,
      int score,
      LocalDateTime updatedAt,
      SolutionStatus status,
      ProgrammingLanguage language,
      String languageVersion,
      Framework framework,
      String frameworkVersion,
      OperatingSystem os,
      String osVersion,
      String probableCause,
      String codeChange) {
    this.id = id;
    this.authorUsername = authorUsername;
    this.explanation = explanation;
    this.score = score;
    this.updatedAt = updatedAt;
    this.status = status;
    this.language = language;
    this.languageVersion = languageVersion;
    this.framework = framework;
    this.frameworkVersion = frameworkVersion;
    this.os = os;
    this.osVersion = osVersion;
    this.probableCause = probableCause;
    this.codeChange = codeChange;
  }
}
