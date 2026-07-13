package com.project.hearmeout_backend.post_service.controller;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.common_lib.exception.PostNotFoundException;
import com.project.hearmeout_backend.common_lib.exception.TagNotFoundException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.post_service.dto.request.ErrorReportSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.dto.response.ErrorReportResponseDTO;
import com.project.hearmeout_backend.post_service.service.implementation.ErrorReportServiceImpl;
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
@RequestMapping("/error-report")
@RequiredArgsConstructor
@Tag(
    name = "Error Report Management",
    description = "Endpoints for creating, reading, updating, and deleting error reports")
public class ErrorReportController {

  private final ErrorReportServiceImpl errorReportServiceImpl;

  @Operation(
      summary = "Submit a new error report",
      description =
          """
          Creates a new error report with title, description, code, and tags. Requires user authentication.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PostMapping
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<@NonNull String> submitErrorReport(
      @Valid @RequestBody ErrorReportSubmitRequestDTO dto,
      @AuthenticationPrincipal CustomUserDetails userDetails)
      throws UserNotFoundException, TagNotFoundException {
    errorReportServiceImpl.submitErrorReport(dto, userDetails.getUserId());

    return ResponseEntity.status(HttpStatus.CREATED).body("Error report created successfully");
  }

  @Operation(
      summary = "Get error report details",
      description =
          "Retrieves a specific error report by its ID, including solutions, comments, and tags.")
  @GetMapping("/{errorReportId}")
  public ResponseEntity<@NonNull ErrorReportResponseDTO> getErrorReport(
      @PathVariable UUID errorReportId,
      @RequestParam(defaultValue = "5") int limit,
      @RequestParam(defaultValue = "0") int offset,
      @AuthenticationPrincipal CustomUserDetails userDetails)
      throws PostNotFoundException {
    UUID userId = userDetails == null ? new UUID(0, 0) : userDetails.getUserId();
    String username = userDetails == null ? "" : userDetails.getUsername();

    return ResponseEntity.status(HttpStatus.OK)
        .body(
            errorReportServiceImpl.getErrorReportDetails(
                errorReportId, userId, username, limit, offset));
  }

  @Operation(
      summary = "Update an error report",
      description =
          """
          Modifies an existing error report. Only the author can update it.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PutMapping("/{errorReportId}")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<@NonNull String> updateErrorReport(
      @PathVariable UUID errorReportId,
      @Valid @RequestBody ErrorReportSubmitRequestDTO dto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    errorReportServiceImpl.updateErrorReport(errorReportId, dto, userDetails.getUserId());

    return ResponseEntity.status(HttpStatus.OK).body("Error report modified successfully");
  }

  @Operation(
      summary = "Delete an error report",
      description =
          """
          Removes an existing error report. Only the author can delete it.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @DeleteMapping("/{errorReportId}")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<@NonNull String> deleteErrorReport(
      @PathVariable UUID errorReportId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    errorReportServiceImpl.deleteErrorReport(errorReportId, userDetails.getUserId());

    return ResponseEntity.status(HttpStatus.OK).body("Error report deleted successfully");
  }
}
