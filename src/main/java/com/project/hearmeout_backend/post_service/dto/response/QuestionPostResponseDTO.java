package com.project.hearmeout_backend.post_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.hearmeout_backend.interaction_service.dto.response.CommentResponseDTO;
import com.project.hearmeout_backend.post_service.model.enums.PostStatus;
import com.project.hearmeout_backend.interaction_service.model.enums.VoteType;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionPostResponseDTO {
    @Schema(description = "The unique identifier of the question post")
    private Long postId;

    @Schema(description = "The title of the question")
    private String title;

    @Schema(description = "The detailed content of the question")
    private String body;

    @Schema(description = "list of answers to the question")
    private List<PostAnswerResponseDTO> answers;

    @Schema(description = "The username of the question's author")
    private String authorUsername;

    @Schema(description = "list of tags associated with the post")
    private List<TagResponseDTO> tags;

    @Schema(description = "Indicates if the currently authenticated user has cast a vote on this question")
    private boolean voted;

    @Schema(description = "The specific type of vote cast by the current user (if voted is true)")
    private VoteType voteType;

    @Schema(description = "list of comments on the post")
    private List<CommentResponseDTO> comments;

    @Schema(description = "timestamp of when the post was created")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDateTime updatedAt;

    @Schema(description = "The current status of the question (e.g., OPEN, ANSWERED, CLOSED)")
    private PostStatus postStatus;

    @Schema(description = "The net vote score of the question (upvotes minus downvotes)")
    private int score;

    @Schema(description = "Indicates whether the current authenticated user has permission to edit or delete this question")
    private boolean operable;

    public QuestionPostResponseDTO(Long postId, String authorUsername, String title, String body, int score, LocalDateTime updatedAt, PostStatus postStatus) {
        this.postId = postId;
        this.authorUsername = authorUsername;
        this.title = title;
        this.body = body;
        this.score = score;
        this.updatedAt = updatedAt;
        this.postStatus = postStatus;
    }
}
