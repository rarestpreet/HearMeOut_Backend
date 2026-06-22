package com.project.hearmeout_backend.interaction_service.controller;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.interaction_service.dto.request.VoteRequestDTO;
import com.project.hearmeout_backend.interaction_service.service.implementation.VoteServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(
        name = "Vote Management",
        description = "Endpoints for casting, modifying, and removing votes on posts"
)
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isFullyAuthenticated() && !hasAuthority('ADMIN')")
public class VoteController {

    private final VoteServiceImpl voteServiceImpl;

    @Operation(
            summary = "Submit or toggle a vote",
            description = "Allows a user to upvote or downvote a post (question or answer). Submitting the same vote again will remove it. Submitting a different vote will change it."
    )
    @PostMapping("/vote")
    public ResponseEntity<@NonNull String> toggleVote(
            @RequestBody VoteRequestDTO voteRequestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        voteServiceImpl
                .handleVote(voteRequestDTO, userDetails.getUserId());

        return ResponseEntity.status(HttpStatus.OK)
                .body("Vote has been updated");
    }
}
