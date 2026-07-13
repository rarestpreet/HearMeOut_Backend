package com.project.hearmeout_backend.user_service.service.implementation;

import com.project.hearmeout_backend.authentication_service.model.enums.RoleType;
import com.project.hearmeout_backend.common_lib.dto.PageData;
import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.common_lib.exception.EmailAlreadyExistException;
import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.common_lib.exception.UserAlreadyExistException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.interaction_service.repository.CommentRepository;
import com.project.hearmeout_backend.post_service.repository.ErrorReportRepository;
import com.project.hearmeout_backend.post_service.repository.SolutionRepository;
import com.project.hearmeout_backend.user_service.dto.request.UserProfileModificationRequestDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserAnswerResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserCommentResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserProfileResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserErrorReportResponseDTO;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {

  private final UserRepository userRepo;
  private final ErrorReportRepository errorReportRepo;
  private final SolutionRepository solutionRepo;
  private final CommentRepository commentRepo;

  public UserProfileResponseDTO getUserProfile(String username, UUID currUserId)
      throws UserNotFoundException {
    UserProfileResponseDTO profileResponse =
        userRepo
            .getUserProfileByUsername(username)
            .orElseThrow(
                () -> new UserNotFoundException("User not found with username: " + username));

    return UserProfileResponseDTO.builder()
        .userId(profileResponse.getUserId())
        .username(profileResponse.getUsername())
        .email(profileResponse.getEmail())
        .reputation(profileResponse.getReputation())
        .createdAt(profileResponse.getCreatedAt())
        .isOperable(profileResponse.getUserId().equals(currUserId))
        .isAccountVerified(profileResponse.isAccountVerified())
        .isAccountTerminated(profileResponse.isAccountTerminated())
        .build();
  }

  @Transactional(readOnly = true)
  public User checkAndGetUserByUserId(UUID userId) throws UserNotFoundException {
    return userRepo
        .findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
  }

  @Transactional(readOnly = true)
  public User checkAndGetUserByUsername(String username) throws UserNotFoundException {
    return userRepo
        .findByUsername(username)
        .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
  }

  @Transactional(readOnly = true)
  public User checkAndGetUserByEmail(String email) throws UserNotFoundException {
    return userRepo
        .findByEmail(email)
        .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
  }

  @Transactional(readOnly = true)
  public PagedResponse<UserErrorReportResponseDTO> getUserErrorReports(
      String username, int limit, int offset) throws UserNotFoundException {
    checkAndGetUserByUsername(username);
    int page = offset / limit;
    Pageable pageable = PageRequest.of(Math.max(page, 0), limit);

    Page<UserErrorReportResponseDTO> userQuestionsPage =
        errorReportRepo.findUserErrorReportsByUsername(username, pageable);

    return PagedResponse.<UserErrorReportResponseDTO>builder()
        .data(userQuestionsPage.getContent())
        .pageData(
            PageData.builder()
                .hasMore(userQuestionsPage.hasNext())
                .total(userQuestionsPage.getTotalElements())
                .offset(offset)
                .limit(limit)
                .build())
        .build();
  }

  @Transactional(readOnly = true)
  public PagedResponse<UserAnswerResponseDTO> getUserSolutions(String username, int limit, int offset)
      throws UserNotFoundException {
    checkAndGetUserByUsername(username);
    int page = offset / limit;
    Pageable pageable = PageRequest.of(Math.max(page, 0), limit);

    Page<UserAnswerResponseDTO> userAnswersPage =
        solutionRepo.findUserSolutionsByUsername(username, pageable);

    return PagedResponse.<UserAnswerResponseDTO>builder()
        .data(userAnswersPage.getContent())
        .pageData(
            PageData.builder()
                .hasMore(userAnswersPage.hasNext())
                .total(userAnswersPage.getTotalElements())
                .offset(offset)
                .limit(limit)
                .build())
        .build();
  }

  @Transactional(readOnly = true)
  public PagedResponse<UserCommentResponseDTO> getUserComments(
      String username, int limit, int offset) throws UserNotFoundException {
    checkAndGetUserByUsername(username);
    int page = offset / limit;
    Pageable pageable = PageRequest.of(Math.max(page, 0), limit);

    Page<UserCommentResponseDTO> userCommentsPage =
        commentRepo.findUserCommentsByUsername(username, pageable);

    return PagedResponse.<UserCommentResponseDTO>builder()
        .data(userCommentsPage.getContent())
        .pageData(
            PageData.builder()
                .hasMore(userCommentsPage.hasNext())
                .total(userCommentsPage.getTotalElements())
                .offset(offset)
                .limit(limit)
                .build())
        .build();
  }

  @Transactional
  public boolean updateUserDetails(UserProfileModificationRequestDTO requestDTO, UUID currUserId)
      throws UserNotFoundException, UserAlreadyExistException, EmailAlreadyExistException {
    User currUser = checkAndGetUserByUserId(currUserId);

    boolean isEmailUpdateRequested = !Objects.equals(currUser.getEmail(), requestDTO.getEmail()),
        isUsernameUpdateRequested =
            !Objects.equals(currUser.getUsername(), requestDTO.getUsername());
    boolean emailUpdateAllowed = currUser.emailUpdateCooldown() == 0,
        usernameUpdateAllowed = currUser.usernameUpdateCooldown() == 0;

    if (!usernameUpdateAllowed
        && !emailUpdateAllowed
        && isEmailUpdateRequested
        && isUsernameUpdateRequested) {
      throw new InvalidOperationException(
          "You changed your email and username recently. Please wait until the cooldown ends.");
    }
    if (!emailUpdateAllowed && isEmailUpdateRequested) {
      throw new InvalidOperationException(
          "You changed your email recently. Please wait until the cooldown ends.");
    }
    if (!usernameUpdateAllowed && isUsernameUpdateRequested) {
      throw new InvalidOperationException(
          "You changed your username recently. Please wait until the cooldown ends.");
    }

    if (isEmailUpdateRequested) {
      if (!userRepo.existsByEmail(requestDTO.getEmail())) {
        currUser.setEmail(requestDTO.getEmail());
        currUser.setAccountVerified(false);
        currUser.setRole(RoleType.USER);
      } else {
        throw new UserAlreadyExistException(
            "User already exist with email: " + requestDTO.getEmail());
      }
    }

    if (isUsernameUpdateRequested) {
      if (!userRepo.existsByUsername(requestDTO.getUsername())) {
        currUser.setUsername(requestDTO.getUsername());
      } else {
        throw new UserAlreadyExistException(
            "User already exist with username: " + requestDTO.getUsername());
      }
    }

    if (!isEmailUpdateRequested && !isUsernameUpdateRequested) {
      return false;
    }

    currUser.markUpdatedAt(isEmailUpdateRequested, isUsernameUpdateRequested);
    userRepo.save(currUser);

    return isEmailUpdateRequested;
  }

  @Transactional
  public void terminateUserAccount(UUID currUserId) throws UserNotFoundException {
    User currUser = checkAndGetUserByUserId(currUserId);

    /*
    update logic for account delete

    currUser.setAccountTerminated(true);
    currUser.setEmail("DELETED" + currUser.getEmail());
    currUser.setAccountVerified(false);
    */

    userRepo.delete(currUser);
  }
}
