package com.project.hearmeout_backend.post_service.controller;

import com.project.hearmeout_backend.post_service.dto.request.TagCreationRequestDTO;
import com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO;
import com.project.hearmeout_backend.post_service.service.implementation.TagServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
@NullMarked
@Tag(
        name = "Tag Management",
        description = "Endpoints for fetching and creating tags used for categorizing posts"
)
@SecurityRequirement(name = "bearerAuth")
public class TagController {
    private final TagServiceImpl tagServiceImpl;

    @Operation(
            summary = "Get all tags",
            description = "Retrieves a paginated list of all available tags in the system."
    )
    @GetMapping("")
    public ResponseEntity<List<TagResponseDTO>> tagList(@RequestParam(defaultValue = "0") int pageNum) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(tagServiceImpl
                        .getAllTags(pageNum)
                );
    }

    @Operation(
            summary = "Create a tag",
            description = "Creates a new tag. Only users with ADMIN authority can perform this action."
    )
    @PreAuthorize("isFullyAuthenticated() && hasAuthority('ADMIN')")
    @PostMapping("")
    public ResponseEntity<String> createTag(@RequestBody TagCreationRequestDTO tag) {
        tagServiceImpl
                .createNewTag(tag);

        return ResponseEntity.status(HttpStatus.OK)
                .body("tag created successfully");
    }
}
