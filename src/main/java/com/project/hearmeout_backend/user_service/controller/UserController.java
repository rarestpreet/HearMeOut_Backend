package com.project.hearmeout_backend.user_service.controller;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.authentication_service.service.implementation.SecurityServiceImpl;
import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.common_lib.exception.EmailAlreadyExistException;
import com.project.hearmeout_backend.common_lib.exception.UserAlreadyExistException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.user_service.dto.request.UserProfileModificationRequestDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserAnswerResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserCommentResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserErrorReportResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserProfileResponseDTO;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile/{username}")
@RequiredArgsConstructor
@Tag(
    name = "User Profile Management",
    description = "Endpoints for viewing and managing user profiles and their activity")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

  private final UserServiceImpl userServiceImpl;
  private final SecurityServiceImpl securityServiceImpl;

  @Operation(
      summary = "Get user profile",
      description =
          """
          Retrieves the public profile information of a user by their username.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: ADMIN, USER, VERIFIED_USER
          - Denied Roles: GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @GetMapping("")
  @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'VERIFIED_USER')")
  public ResponseEntity<@NonNull UserProfileResponseDTO> userProfile(
      @PathVariable String username, @AuthenticationPrincipal CustomUserDetails userDetails)
      throws UserNotFoundException {
    UserProfileResponseDTO profile =
        userServiceImpl.getUserProfile(
            username, userDetails == null ? null : userDetails.getUserId());

    return ResponseEntity.status(HttpStatus.OK).body(profile);
  }

  // add pagination and sorting (from recent to older)
  @Operation(
      summary = "Get user error reports",
      description =
          """
          Retrieves a list of all error reports asked by the specified user.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: ADMIN, VERIFIED_USER
          - Denied Roles: USER, GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @GetMapping("/error-reports")
  @PreAuthorize("hasAnyAuthority('ADMIN', 'VERIFIED_USER')")
  public ResponseEntity<@NonNull PagedResponse<UserErrorReportResponseDTO>> userErrorReports(
      @PathVariable String username,
      @RequestParam(defaultValue = "5") int limit,
      @RequestParam(defaultValue = "0") int offset)
      throws UserNotFoundException {
    PagedResponse<UserErrorReportResponseDTO> userQuestions =
        userServiceImpl.getUserErrorReports(username, limit, offset);

    return ResponseEntity.status(HttpStatus.OK).body(userQuestions);
  }

  // add pagination and sorting (from recent to older)
  @Operation(
      summary = "Get user solutions",
      description =
          """
          Retrieves a list of all solutions provided by the specified user.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: ADMIN, VERIFIED_USER
          - Denied Roles: USER, GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @GetMapping("/solutions")
  @PreAuthorize("hasAnyAuthority('ADMIN', 'VERIFIED_USER')")
  public ResponseEntity<@NonNull PagedResponse<UserAnswerResponseDTO>> userSolutions(
      @PathVariable String username,
      @RequestParam(defaultValue = "5") int limit,
      @RequestParam(defaultValue = "0") int offset)
      throws UserNotFoundException {
    PagedResponse<UserAnswerResponseDTO> userAnswer =
        userServiceImpl.getUserSolutions(username, limit, offset);

    return ResponseEntity.status(HttpStatus.OK).body(userAnswer);
  }

  // add pagination and sorting (from recent to older)
  @Operation(
      summary = "Get user comments",
      description =
          """
          Retrieves a list of all comments made by the specified user.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: ADMIN, VERIFIED_USER
          - Denied Roles: USER, GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @GetMapping("/comments")
  @PreAuthorize("hasAnyAuthority('ADMIN', 'VERIFIED_USER')")
  public ResponseEntity<@NonNull PagedResponse<UserCommentResponseDTO>> userComments(
      @PathVariable String username,
      @RequestParam(defaultValue = "5") int limit,
      @RequestParam(defaultValue = "0") int offset)
      throws UserNotFoundException {
    PagedResponse<UserCommentResponseDTO> comments =
        userServiceImpl.getUserComments(username, limit, offset);

    return ResponseEntity.status(HttpStatus.OK).body(comments);
  }

  @Operation(
      summary = "Update user profile",
      description =
          """
          Modifies the authenticated user's profile details such as username and email. Terminates the current session upon success.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          - Denied Roles: ADMIN, USER, GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @PutMapping("")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  public ResponseEntity<@NonNull String> updateUserProfile(
      @PathVariable String username,
      @Valid @RequestBody UserProfileModificationRequestDTO userProfileModificationRequestDTO,
      @AuthenticationPrincipal CustomUserDetails currUser)
      throws UserNotFoundException, EmailAlreadyExistException, UserAlreadyExistException {
    boolean emailChanged =
        userServiceImpl.updateUserDetails(userProfileModificationRequestDTO, currUser.getUserId());

    if (emailChanged) {
      List<ResponseCookie> clearedCookie =
          securityServiceImpl.terminateSession(currUser.getUsername());

      return ResponseEntity.status(HttpStatus.OK)
          .header(
              HttpHeaders.SET_COOKIE,
              clearedCookie.get(0).toString(),
              clearedCookie.get(1).toString())
          .body("Details updated Successfully");
    }

    return ResponseEntity.status(HttpStatus.OK).body("Details updated Successfully");
  }

  @Operation(
      summary = "Delete user account",
      description =
          """
          Permanently deletes the authenticated user's account and terminates their current session.

          **Access Control**
          - Authentication: Required
          - Allowed Roles: VERIFIED_USER
          - Denied Roles: ADMIN, USER, GUEST
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  @DeleteMapping("")
  @PreAuthorize("isFullyAuthenticated() && hasAuthority('VERIFIED_USER')")
  public ResponseEntity<@NonNull String> deleteUser(
      @PathVariable String username, @AuthenticationPrincipal CustomUserDetails currUser)
      throws UserNotFoundException {
    userServiceImpl.terminateUserAccount(currUser.getUserId());

    List<ResponseCookie> clearedCookie =
        securityServiceImpl.terminateSession(currUser.getUsername());

    return ResponseEntity.status(HttpStatus.OK)
        .header(
            HttpHeaders.SET_COOKIE,
            clearedCookie.get(0).toString(),
            clearedCookie.get(1).toString())
        .body("Account deleted Successfully");
  }
}
