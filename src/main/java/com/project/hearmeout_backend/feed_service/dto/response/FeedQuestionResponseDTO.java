package com.project.hearmeout_backend.feed_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO;
import com.project.hearmeout_backend.post_service.model.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedQuestionResponseDTO {
  @Schema(description = "The unique identifier of the question post, used for navigation")
  private Long navigationPostId;

  @Schema(description = "The username of the question's author")
  private String authorUsername;

  @Schema(description = "The title of the question")
  private String title;

  @Schema(description = "The net vote score of the question (upvotes minus downvotes)")
  private int score;

  @Schema(description = "timestamp of when the post was created")
  @JsonFormat(pattern = "dd-MM-yyyy")
  private LocalDateTime updatedAt;

  @Schema(description = "The current status of the question (e.g., OPEN, ANSWERED)")
  private PostStatus postStatus;

  @Schema(description = "list of tags associated with the post")
  private List<TagResponseDTO> tags;

  public FeedQuestionResponseDTO(
      Long navigationPostId,
      String authorUsername,
      String title,
      int score,
      LocalDateTime updatedAt,
      PostStatus postStatus) {
    this.navigationPostId = navigationPostId;
    this.authorUsername = authorUsername;
    this.title = title;
    this.score = score;
    this.updatedAt = updatedAt;
    this.postStatus = postStatus;
  }
}
