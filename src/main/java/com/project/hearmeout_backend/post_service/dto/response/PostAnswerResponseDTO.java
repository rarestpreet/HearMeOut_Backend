package com.project.hearmeout_backend.post_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.hearmeout_backend.interaction_service.dto.response.CommentResponseDTO;
import com.project.hearmeout_backend.post_service.model.enums.PostStatus;
import com.project.hearmeout_backend.interaction_service.model.enums.VoteType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostAnswerResponseDTO {
    @Schema(description = "The unique identifier of the answer post")
    private Long postId;

    @Schema(description = "Indicates if the currently authenticated user has cast a vote on this answer")
    private boolean voted;

    @Schema(description = "The specific type of vote cast by the current user (if voted is true)")
    private VoteType voteType;

    @Schema(description = "The username of the answer's author")
    private String authorUsername;

    @Schema(description = "The detailed content of the answer")
    private String body;

    @Schema(description = "timestamp of when the answer was created")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDateTime updatedAt;

    @Schema(description = "The current status of the answer (e.g., ACCEPTED)")
    private PostStatus postStatus;

    @Schema(description = "list of comments on the answer")
    private List<CommentResponseDTO> comments;

    @Schema(description = "The net vote score of the answer (upvotes minus downvotes)")
    private int score;

    @Schema(description = "Indicates whether the current authenticated user has permission to edit or delete this answer")
    private boolean operable;

    public PostAnswerResponseDTO(Long postId, String authorUsername, String body, int score, LocalDateTime updatedAt, PostStatus postStatus) {
        this.postId = postId;
        this.authorUsername = authorUsername;
        this.body = body;
        this.score = score;
        this.updatedAt = updatedAt;
        this.postStatus = postStatus;
    }
}
