package com.project.hearmeout_backend.post_service.controller;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.common_lib.exception.PostNotFoundException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.post_service.dto.request.AcceptSolutionRequestDTO;
import com.project.hearmeout_backend.post_service.dto.request.SolutionSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.dto.response.SolutionResponseDTO;
import com.project.hearmeout_backend.post_service.service.implementation.SolutionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("solution")
@RequiredArgsConstructor
@Tag(
    name = "Solution Management",
    description = "Endpoints for creating, reading, updating, and deleting solutions")
public class SolutionController {

  private final SolutionServiceImpl solutionServiceImpl;

  @Operation(
      summary = "Submit a solution",
      description =
          """
          Adds a new solution to a specific error report. Requires user authentication.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PostMapping("error-report/{errorReportId}")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<@NonNull String> submitSolution(
      @PathVariable UUID errorReportId,
      @Valid @RequestBody SolutionSubmitRequestDTO dto,
      @AuthenticationPrincipal CustomUserDetails userDetails)
      throws UserNotFoundException, PostNotFoundException {
    solutionServiceImpl.submitSolution(errorReportId, dto, userDetails.getUserId());

    return ResponseEntity.status(HttpStatus.CREATED).body("Solution created successfully");
  }

  @Operation(
      summary = "Get paginated solutions for an error report",
      description =
          "Retrieves paginated solutions for a specific error report. User context is applied if authenticated.")
  @GetMapping("error-report/{errorReportId}")
  public ResponseEntity<@NonNull PagedResponse<SolutionResponseDTO>> getSolutions(
      @PathVariable UUID errorReportId,
      @RequestParam(defaultValue = "5") int limit,
      @RequestParam(defaultValue = "0") int offset,
      @AuthenticationPrincipal CustomUserDetails userDetails)
      throws PostNotFoundException {
    UUID userId = userDetails == null ? new UUID(0, 0) : userDetails.getUserId();
    String username = userDetails == null ? "" : userDetails.getUsername();

    return ResponseEntity.status(HttpStatus.OK)
        .body(solutionServiceImpl.getSolutions(errorReportId, userId, username, limit, offset));
  }

  @Operation(
      summary = "Accept/toggle a solution",
      description =
          """
          Toggles the accepted status of a solution. Only the error report author can accept a solution.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PostMapping("toggleStatus")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<@NonNull String> toggleSolutionStatus(
      @Valid @RequestBody AcceptSolutionRequestDTO dto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    solutionServiceImpl.handleSolutionStatus(dto, userDetails.getUserId());

    return ResponseEntity.status(HttpStatus.OK).body("Solution status updated successfully");
  }

  @Operation(
      summary = "Update a solution",
      description =
          """
          Modifies an existing solution. Only the author can update it.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PutMapping("{solutionId}")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<@NonNull String> updateSolution(
      @PathVariable UUID solutionId,
      @Valid @RequestBody SolutionSubmitRequestDTO dto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    solutionServiceImpl.updateSolution(solutionId, dto, userDetails.getUserId());

    return ResponseEntity.status(HttpStatus.OK).body("Solution modified successfully");
  }

  @Operation(
      summary = "Delete a solution",
      description =
          """
          Removes an existing solution. Only the author can delete it.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @DeleteMapping("{solutionId}")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<@NonNull String> deleteSolution(
      @PathVariable UUID solutionId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    solutionServiceImpl.deleteSolution(solutionId, userDetails.getUserId());

    return ResponseEntity.status(HttpStatus.OK).body("Solution deleted successfully");
  }
}
