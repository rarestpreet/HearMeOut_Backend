package com.project.hearmeout_backend.user_service.service.implementation;

import com.project.hearmeout_backend.common_lib.exception.EmailAlreadyExistException;
import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.common_lib.exception.UserAlreadyExistException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.interaction_service.repository.CommentRepository;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.post_service.repository.PostRepository;
import com.project.hearmeout_backend.user_service.dto.request.UserProfileModificationRequestDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserAnswerResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserCommentResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserProfileResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserQuestionResponseDTO;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final UserRepository userRepo;
    private final PostRepository postRepo;
    private final CommentRepository commentRepo;

    public UserProfileResponseDTO getUserProfile(String username, Long currUserId)
            throws UserNotFoundException {
        UserProfileResponseDTO profileDTO = userRepo.getUserProfileByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        profileDTO.setOperable(profileDTO.getUserId().equals(currUserId));

        return profileDTO;
    }

    @Transactional(readOnly = true)
    public User checkAndGetUserByUserId(Long userId)
            throws UserNotFoundException {
        return userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    @Transactional(readOnly = true)
    public User checkAndGetUserByUsername(String username)
            throws UserNotFoundException {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public User checkAndGetUserByEmail(String email)
            throws UserNotFoundException {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public List<UserQuestionResponseDTO> getUserQuestions(String username)
            throws UserNotFoundException {
        checkAndGetUserByUsername(username);

        return postRepo.findUserQuestionByUsername(username, PostType.QUESTION);
    }

    @Transactional(readOnly = true)
    public List<UserAnswerResponseDTO> getUserAnswers(String username)
            throws UserNotFoundException {
        checkAndGetUserByUsername(username);

        return postRepo.findUserAnswerByUsername(username, PostType.ANSWER);
    }

    @Transactional(readOnly = true)
    public List<UserCommentResponseDTO> getUserComments(String username)
            throws UserNotFoundException {
        checkAndGetUserByUsername(username);

        return commentRepo.findUserCommentsByUsername(username);
    }

    @Transactional
    public boolean updateUserDetails(UserProfileModificationRequestDTO requestDTO, Long currUserId)
            throws UserNotFoundException, UserAlreadyExistException, EmailAlreadyExistException {
        User currUser = checkAndGetUserByUserId(currUserId);

        boolean isEmailUpdateRequested = !Objects.equals(currUser.getEmail(), requestDTO.getEmail()),
                isUsernameUpdateRequested = !Objects.equals(currUser.getUsername(), requestDTO.getUsername());
        boolean emailUpdateAllowed = currUser.emailUpdateCooldown() == 0,
                usernameUpdateAllowed = currUser.usernameUpdateCooldown() == 0;

        if (!usernameUpdateAllowed && !emailUpdateAllowed && isEmailUpdateRequested && isUsernameUpdateRequested) {
            throw new InvalidOperationException(
                    "You changed your email and username recently. Please wait until the cooldown ends."
            );
        }
        if (!emailUpdateAllowed && isEmailUpdateRequested) {
            throw new InvalidOperationException(
                    "You changed your email recently. Please wait until the cooldown ends."
            );
        }
        if (!usernameUpdateAllowed && isUsernameUpdateRequested) {
            throw new InvalidOperationException(
                    "You changed your username recently. Please wait until the cooldown ends."
            );
        }

        if (isEmailUpdateRequested) {
            if (!userRepo.existsByEmail(requestDTO.getEmail())) {
                currUser.setEmail(requestDTO.getEmail());
                currUser.setAccountVerified(false);
            } else {
                throw new UserAlreadyExistException("User already exist with email: " + requestDTO.getEmail());
            }
        }

        if (isUsernameUpdateRequested) {
            if (!userRepo.existsByUsername(requestDTO.getUsername())) {
                currUser.setUsername(requestDTO.getUsername());
            } else {
                throw new UserAlreadyExistException("User already exist with username: " + requestDTO.getUsername());
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
    public void terminateUserAccount(Long currUserId)
            throws UserNotFoundException {
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
