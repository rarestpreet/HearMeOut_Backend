package com.project.hearmeout_backend.user_service.controller;

import com.project.hearmeout_backend.user_service.dto.request.UserProfileModificationRequestDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserAnswerResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserCommentResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserProfileResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserQuestionResponseDTO;
import com.project.hearmeout_backend.common_lib.exception.EmailAlreadyExistException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.common_lib.exception.UserAlreadyExistException;
import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.authentication_service.service.implementation.SecurityServiceImpl;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profile/{username}")
@RequiredArgsConstructor
@Tag(name = "User Profile Management", description = "Endpoints for viewing and managing user profiles and their activity")
public class UserController {

    private final UserServiceImpl userServiceImpl;
    private final SecurityServiceImpl securityServiceImpl;

    @Operation(summary = "Get user profile", description = "Retrieves the public profile information of a user by their username.")
    @GetMapping("")
    public ResponseEntity<@NonNull UserProfileResponseDTO> userProfile(@PathVariable String username,
                                                                       @AuthenticationPrincipal CustomUserDetails userDetails)
            throws UserNotFoundException {
        UserProfileResponseDTO profile = userServiceImpl.getUserProfile(username, userDetails == null ? null : userDetails.getUserId());

        return ResponseEntity.status(HttpStatus.OK).body(profile);
    }

    // add pagination and sorting (from recent to older)
    @Operation(summary = "Get user questions", description = "Retrieves a list of all questions asked by the specified user.")
    @GetMapping("/questions")
    public ResponseEntity<@NonNull List<UserQuestionResponseDTO>> userQuestions(@PathVariable String username)
            throws UserNotFoundException {
        List<UserQuestionResponseDTO> userQuestions = userServiceImpl.getUserQuestions(username);

        return ResponseEntity.status(HttpStatus.OK).body(userQuestions);
    }

    // add pagination and sorting (from recent to older)
    @Operation(summary = "Get user answers", description = "Retrieves a list of all answers provided by the specified user.")
    @GetMapping("/answers")
    public ResponseEntity<@NonNull List<UserAnswerResponseDTO>> userAnswers(@PathVariable String username)
            throws UserNotFoundException {
        List<UserAnswerResponseDTO> userAnswer = userServiceImpl.getUserAnswers(username);

        return ResponseEntity.status(HttpStatus.OK).body(userAnswer);
    }

    // add pagination and sorting (from recent to older)
    @Operation(summary = "Get user comments", description = "Retrieves a list of all comments made by the specified user.")
    @GetMapping("/comments")
    public ResponseEntity<@NonNull List<UserCommentResponseDTO>> userComments(@PathVariable String username)
            throws UserNotFoundException {
        List<UserCommentResponseDTO> comments = userServiceImpl.getUserComments(username);

        return ResponseEntity.status(HttpStatus.OK).body(comments);
    }

    @Operation(summary = "Update user profile", description = "Modifies the authenticated user's profile details such as username and email. Terminates the current session upon success.")
    @PutMapping("")
    @PreAuthorize("isFullyAuthenticated() && !hasAuthority('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<@NonNull String> updateUserProfile(@PathVariable String username,
                                                             @Valid @RequestBody UserProfileModificationRequestDTO userProfileModificationRequestDTO,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails)
            throws UserNotFoundException, EmailAlreadyExistException, UserAlreadyExistException {
        userServiceImpl.updateUserDetails(userProfileModificationRequestDTO, userDetails.getUserId());

        ResponseCookie clearedCookie = securityServiceImpl.terminateSession();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, clearedCookie.toString())
                .body("Details updated Successfully");
    }

    @Operation(summary = "Delete user account", description = "Permanently deletes the authenticated user's account and terminates their current session.")
    @DeleteMapping("")
    @PreAuthorize("isFullyAuthenticated() && !hasAuthority('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<@NonNull String> deleteUser(@PathVariable String username,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails)
            throws UserNotFoundException {
        userServiceImpl.terminateUserAccount(userDetails.getUserId());

        ResponseCookie clearedCookie = securityServiceImpl.terminateSession();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, clearedCookie.toString())
                .body("Account deleted Successfully");
    }
}
