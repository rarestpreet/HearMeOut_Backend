package com.project.hearmeout_backend.interaction_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CommentResponseDTO {
    @Schema(description = "The unique identifier of the comment")
    private Long commentId;

    @Schema(description = "The text content of the comment")
    private String body;

    @Schema(description = "The username of the comment's author")
    private String authorUsername;

    @Schema(description = "The ID of the parent post (question or answer) for navigation purposes")
    private Long navigationPostId;

    @Schema(description = "timestamp of the last update")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDateTime updatedAt;

    @Schema(description = "Indicates whether the current authenticated user has permission to edit or delete this comment")
    private boolean operable;

    public CommentResponseDTO(Long commentId, String body, String authorUsername, Long navigationPostId, LocalDateTime updatedAt) {
        this.commentId = commentId;
        this.body = body;
        this.authorUsername = authorUsername;
        this.navigationPostId = navigationPostId;
        this.updatedAt = updatedAt;
    }
}
