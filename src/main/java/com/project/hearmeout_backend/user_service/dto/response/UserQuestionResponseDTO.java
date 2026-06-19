package com.project.hearmeout_backend.user_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.hearmeout_backend.post_service.model.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class UserQuestionResponseDTO {
    @Schema(description = "The unique identifier of the question post, used for navigation")
    private Long navigationPostId;

    @Schema(description = "The title of the question")
    private String title;

    @Schema(description = "The net vote score of the question")
    private int score;

    @Schema(description = "timestamp of when the question was modified (or created)")
    @JsonFormat(pattern = "dd-MM-yyy")
    private LocalDateTime updatedAt;

    @Schema(description = "The current status of the question (e.g., OPEN, CLOSED)")
    private PostStatus postStatus;
}

