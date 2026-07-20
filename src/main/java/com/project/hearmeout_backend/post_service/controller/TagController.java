package com.project.hearmeout_backend.post_service.controller;

import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO;
import com.project.hearmeout_backend.post_service.service.implementation.TagServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("tag")
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
}
