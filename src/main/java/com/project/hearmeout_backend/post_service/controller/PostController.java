package com.project.hearmeout_backend.post_service.controller;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.common_lib.exception.PostNotFoundException;
import com.project.hearmeout_backend.common_lib.exception.TagNotFoundException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.post_service.dto.request.AcceptAnswerRequestDTO;
import com.project.hearmeout_backend.post_service.dto.request.AnswerSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.dto.request.QuestionSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.dto.response.PostAnswerResponseDTO;
import com.project.hearmeout_backend.post_service.dto.response.QuestionPostResponseDTO;
import com.project.hearmeout_backend.post_service.service.implementation.PostServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@Tag(
    name = "Post Management",
    description = "Endpoints for creating, reading, updating, and deleting questions and answers")
public class PostController {

  private final PostServiceImpl postServiceImpl;

  @Operation(
      summary = "Ask a new question",
      description =
          """
          Creates a new question post with title, body, and tags. Requires user authentication.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          - Denied Roles: ADMIN, USER, GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PostMapping("/ask")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<@NonNull String> askQuestion(
      @Valid @RequestBody QuestionSubmitRequestDTO questionSubmitRequestDTO,
      @AuthenticationPrincipal CustomUserDetails userDetails)
      throws UserNotFoundException, TagNotFoundException {
    postServiceImpl.postNewQuestion(questionSubmitRequestDTO, userDetails.getUserId());

    return ResponseEntity.status(HttpStatus.CREATED).body("Question created successfully");
  }

  @Operation(
      summary = "Submit an answer",
      description =
          """
          Adds a new answer to a specific question. Requires user authentication.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          - Denied Roles: ADMIN, USER, GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PostMapping("/{postId}/answer")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<@NonNull String> submitAnswer(
      @PathVariable Long postId,
      @Valid @RequestBody AnswerSubmitRequestDTO answerSubmitRequestDTO,
      @AuthenticationPrincipal CustomUserDetails userDetails)
      throws UserNotFoundException, PostNotFoundException {
    postServiceImpl.postNewAnswer(postId, answerSubmitRequestDTO, userDetails.getUserId());

    return ResponseEntity.status(HttpStatus.CREATED).body("Answer created successfully");
  }

  @Operation(
      summary = "Get question details",
      description =
          "Retrieves a specific question by its ID, including its answers, comments, and tags. User context is applied if authenticated.")
  @GetMapping("/{postId}")
  public ResponseEntity<@NonNull QuestionPostResponseDTO> getQuestion(
      @PathVariable Long postId,
      @RequestParam(defaultValue = "5") int limit,
      @RequestParam(defaultValue = "0") int offset,
      @AuthenticationPrincipal CustomUserDetails userDetails)
      throws PostNotFoundException {
    Long userId = userDetails == null ? 0L : userDetails.getUserId();
    String username = userDetails == null ? "" : userDetails.getUsername();

    return ResponseEntity.status(HttpStatus.OK)
        .body(postServiceImpl.getQuestionPost(postId, userId, username, limit, offset));
  }

  @Operation(
      summary = "Get paginated answers for a question",
      description =
          "Retrieves paginated answers for a specific question. User context is applied if authenticated.")
  @GetMapping("/{postId}/answers")
  public ResponseEntity<@NonNull PagedResponse<PostAnswerResponseDTO>> getAnswers(
      @PathVariable Long postId,
      @RequestParam(defaultValue = "5") int limit,
      @RequestParam(defaultValue = "0") int offset,
      @AuthenticationPrincipal CustomUserDetails userDetails)
      throws PostNotFoundException {
    Long userId = userDetails == null ? 0L : userDetails.getUserId();
    String username = userDetails == null ? "" : userDetails.getUsername();

    return ResponseEntity.status(HttpStatus.OK)
        .body(postServiceImpl.getPostAnswers(postId, userId, username, limit, offset));
  }

  @Operation(
      summary = "Accept an answer",
      description =
          """
          Toggles the accepted status of an answer. Only the question author can accept an answer.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          - Denied Roles: ADMIN, USER, GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PostMapping("/toggleStatus")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<@NonNull String> toggleAnswerStatus(
      @Valid @RequestBody AcceptAnswerRequestDTO acceptAnswerRequestDTO,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    postServiceImpl.handleAnswerStatus(acceptAnswerRequestDTO, userDetails.getUserId());

    return ResponseEntity.status(HttpStatus.CREATED).body("Answer accepted successfully");
  }

  @Operation(
      summary = "Update an answer",
      description =
          """
          Modifies the content of an existing answer. Only the answer author can update it.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          - Denied Roles: ADMIN, USER, GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PutMapping("/answer/{answerId}")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<@NonNull String> modifyAnswer(
      @PathVariable Long answerId,
      @Valid @RequestBody AnswerSubmitRequestDTO answerSubmitRequestDTO,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    postServiceImpl.updateAnswer(answerId, answerSubmitRequestDTO, userDetails.getUserId());

    return ResponseEntity.status(HttpStatus.OK).body("Answer modified successfully");
  }

  @Operation(
      summary = "Delete an answer",
      description =
          """
          Removes an existing answer by its ID. Only the answer author can delete it.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          - Denied Roles: ADMIN, USER, GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @DeleteMapping("/answer/{answerId}")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<@NonNull String> removeAnswer(
      @PathVariable Long answerId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    postServiceImpl.deleteAnswer(answerId, userDetails.getUserId());

    return ResponseEntity.status(HttpStatus.OK).body("Answer deleted successfully");
  }

  @Operation(
      summary = "Update a question",
      description =
          """
          Modifies an existing question's title, body, and tags. Only the question author can update it.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          - Denied Roles: ADMIN, USER, GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PutMapping("/question/{questionId}")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<@NonNull String> modifyQuestion(
      @PathVariable Long questionId,
      @Valid @RequestBody QuestionSubmitRequestDTO questionSubmitRequestDTO,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    postServiceImpl.updateQuestion(questionId, questionSubmitRequestDTO, userDetails.getUserId());

    return ResponseEntity.status(HttpStatus.OK).body("Question modified successfully");
  }

  @Operation(
      summary = "Delete a question",
      description =
          """
          Removes an existing question by its ID. Only the question author can delete it.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          - Denied Roles: ADMIN, USER, GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @DeleteMapping("/question/{questionId}")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<@NonNull String> removeQuestion(
      @PathVariable Long questionId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    postServiceImpl.deleteQuestion(questionId, userDetails.getUserId());

    return ResponseEntity.status(HttpStatus.OK).body("Question deleted successfully");
  }
}
