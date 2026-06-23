package com.project.hearmeout_backend.interaction_service.controller;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.common_lib.exception.CommentNotFoundException;
import com.project.hearmeout_backend.common_lib.exception.PostNotFoundException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.interaction_service.dto.request.CommentRequestDTO;
import com.project.hearmeout_backend.interaction_service.service.implementation.CommentServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Tag(
        name = "Comment Management",
        description = "Endpoints for creating, updating, and deleting comments on posts"
)
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isFullyAuthenticated() && hasAnyAuthority('VERIFIED_USER', 'USER')")
public class CommentController {

    private final CommentServiceImpl commentServiceImpl;

    @Operation(
            summary = "Add a comment",
            description = "Creates a new comment on a specific post. The user must be authenticated."
    )
    @PostMapping("")
    public ResponseEntity<@NonNull String> postComment(
            @Valid @RequestBody CommentRequestDTO commentRequestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws UserNotFoundException, PostNotFoundException {
        commentServiceImpl
                .createNewComment(commentRequestDTO, userDetails.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Comment was added successfully");
    }

    @Operation(
            summary = "Delete a comment",
            description = "Removes an existing comment by its ID. Only the author of the comment can delete it."
    )
    @DeleteMapping("/{commentId}")
    public ResponseEntity<@NonNull String> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws CommentNotFoundException {
        commentServiceImpl
                .removeComment(commentId, userDetails.getUserId());

        return ResponseEntity.status(HttpStatus.OK)
                .body("Comment was deleted successfully");
    }

    @Operation(
            summary = "Update a comment",
            description = "Modifies the content of an existing comment. Only the author can update their own comment."
    )
    @PutMapping("/{commentId}")
    public ResponseEntity<@NonNull String> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequestDTO commentRequestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws CommentNotFoundException {
        commentServiceImpl
                .updateCommentBody(commentId, commentRequestDTO.getBody(), userDetails.getUserId());

        return ResponseEntity.status(HttpStatus.OK)
                .body("Comment was updated successfully");
    }
}
