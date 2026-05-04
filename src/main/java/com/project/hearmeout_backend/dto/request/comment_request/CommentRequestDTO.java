package com.project.hearmeout_backend.dto.request.comment_request;

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
public class CommentRequestDTO {

    @NotBlank(message = "Comment body is required")
    @Size(min = 20, max = 200, message = "Comment must be between 10 and 100 characters")
    @Schema(description = "The text content of the comment")
    private String body;

    @NotNull(message = "Post id is required")
    @Schema(description = "The ID of the post (question or answer) this comment is attached to")
    private Long postId;
}
