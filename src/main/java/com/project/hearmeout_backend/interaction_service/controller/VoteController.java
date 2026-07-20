package com.project.hearmeout_backend.interaction_service.controller;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.interaction_service.dto.request.VoteRequestDTO;
import com.project.hearmeout_backend.interaction_service.model.enums.VoteType;
import com.project.hearmeout_backend.interaction_service.service.implementation.VoteServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(
    name = "Vote Management",
    description = "Endpoints for casting, modifying, and removing votes on posts")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isFullyAuthenticated() && !hasAnyAuthority('ADMIN', 'USER')")
public class VoteController {

  private final VoteServiceImpl voteServiceImpl;

  @Operation(
      summary = "Submit or toggle a vote",
      description =
          """
          Allows a user to upvote or downvote a post (question or answer). Submitting the same vote again will remove it. Submitting a different vote will change it.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          - Denied Roles: ADMIN, USER, GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PostMapping("vote")
  public ResponseEntity<@NonNull String> toggleVote(
      @Valid @RequestBody VoteRequestDTO voteRequestDTO,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam VoteType voteType)
      throws BadRequestException {
    voteServiceImpl.handleVote(voteRequestDTO, userDetails.getUserId(), voteType);

    return ResponseEntity.status(HttpStatus.OK).body("Vote has been updated");
  }
}
