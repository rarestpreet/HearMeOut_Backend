package com.project.hearmeout_backend.post_service.controller;

import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.post_service.dto.request.TagCreationRequestDTO;
import com.project.hearmeout_backend.post_service.dto.request.TagModificationRequestDTO;
import com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO;
import com.project.hearmeout_backend.post_service.service.implementation.TagServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
@NullMarked
@Tag(
    name = "Tag Management",
    description =
        "Endpoints for fetching, creating, updating, and deleting tags used for categorizing posts")
public class TagController {
  private final TagServiceImpl tagServiceImpl;

  @Operation(
      summary = "Get all tags",
      description = "Retrieves a paginated list of all available tags in the system.")
  @GetMapping("")
  public ResponseEntity<PagedResponse<TagResponseDTO>> tagList(
      @RequestParam(defaultValue = "5") int limit, @RequestParam(defaultValue = "0") int offset) {
    return ResponseEntity.status(HttpStatus.OK).body(tagServiceImpl.getAllTags(limit, offset));
  }

  @Operation(
      summary = "Get all tags for admins",
      description =
          """
      Retrieves a paginated list of all available tags in the system including their approval status.

      **Access Control**
      - Authentication: Required
      - Allowed Roles: ADMIN
      - Denied Roles: VERIFIED_USER, USER, GUEST
      """)
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Tags retrieved successfully"),
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required")
  })
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('ADMIN')")
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/admin")
  public ResponseEntity<
          PagedResponse<
              com.project.hearmeout_backend.post_service.dto.response.AdminTagResponseDTO>>
      adminTagList(
          @RequestParam(defaultValue = "5") int limit,
          @RequestParam(defaultValue = "0") int offset) {
    return ResponseEntity.status(HttpStatus.OK).body(tagServiceImpl.getAllTagsAdmin(limit, offset));
  }

  @Operation(
      summary = "Create a tag",
      description =
          """
      Creates a new tag. Only users with ADMIN authority can perform this action.

      **Access Control**
      - Authentication: Required
      - Allowed Roles: ADMIN
      - Denied Roles: VERIFIED_USER, USER, GUEST
      """)
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Tag created successfully"),
    @ApiResponse(responseCode = "400", description = "Validation failed"),
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required")
  })
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('ADMIN')")
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("")
  public ResponseEntity<String> createTag(@Valid @RequestBody TagCreationRequestDTO tag) {
    tagServiceImpl.createNewTag(tag);

    return ResponseEntity.status(HttpStatus.OK).body("tag created successfully");
  }

  @Operation(
      summary = "Update a tag",
      description =
          """
      Updates an existing tag's name and/or description. Only users with ADMIN authority can perform this action. Supports partial updates — only non-null fields are applied.

      **Access Control**
      - Authentication: Required
      - Allowed Roles: ADMIN
      - Denied Roles: VERIFIED_USER, USER, GUEST
      """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('ADMIN')")
  @SecurityRequirement(name = "bearerAuth")
  @PutMapping("{tagId}")
  public ResponseEntity<String> updateTag(
      @Parameter(description = "The ID of the tag to update", required = true) @PathVariable
          UUID tagId,
      @Valid @RequestBody TagModificationRequestDTO tagModificationRequestDTO) {
    tagServiceImpl.updateTag(tagId, tagModificationRequestDTO);

    return ResponseEntity.status(HttpStatus.OK).body("tag updated successfully");
  }

  @Operation(
      summary = "Delete a tag",
      description =
          """
      Permanently removes a tag from the system. Only users with ADMIN authority can perform this action.

      **Access Control**
      - Authentication: Required
      - Allowed Roles: ADMIN
      - Denied Roles: VERIFIED_USER, USER, GUEST
      """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('ADMIN')")
  @SecurityRequirement(name = "bearerAuth")
  @DeleteMapping("{tagId}")
  public ResponseEntity<String> deleteTag(
      @Parameter(description = "The ID of the tag to delete", required = true) @PathVariable
          UUID tagId) {
    tagServiceImpl.deleteTag(tagId);

    return ResponseEntity.status(HttpStatus.OK).body("tag deleted successfully");
  }
}
