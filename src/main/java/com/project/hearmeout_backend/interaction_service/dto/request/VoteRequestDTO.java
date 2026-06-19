package com.project.hearmeout_backend.interaction_service.dto.request;

import com.project.hearmeout_backend.interaction_service.model.enums.VoteType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VoteRequestDTO {

    @NotNull(message = "postId cannot be null")
    @Schema(description = "The ID of the post (question or answer) being voted on")
    private Long postId;

    @NotNull(message = "define the type of vote")
    @Schema(description = "The type of vote being cast (UPVOTE or DOWNVOTE)")
    private VoteType voteType;
}
