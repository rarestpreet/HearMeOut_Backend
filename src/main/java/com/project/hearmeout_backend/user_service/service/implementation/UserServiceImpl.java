package com.project.hearmeout_backend.user_service.service.implementation;

import com.project.hearmeout_backend.user_service.dto.request.UserProfileModificationRequestDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserAnswerResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserCommentResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserProfileResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserQuestionResponseDTO;
import com.project.hearmeout_backend.common_lib.exception.EmailAlreadyExistException;
import com.project.hearmeout_backend.common_lib.exception.UserAlreadyExistException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.interaction_service.repository.CommentRepository;
import com.project.hearmeout_backend.post_service.repository.PostRepository;
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
    public void updateUserDetails(UserProfileModificationRequestDTO requestDTO, Long currUserId)
            throws UserNotFoundException, UserAlreadyExistException, EmailAlreadyExistException {
        User currUser = checkAndGetUserByUserId(currUserId);
        boolean updated = false;

        if (!Objects.equals(currUser.getEmail(), requestDTO.getEmail())) {
            if (!userRepo.existsByEmail(requestDTO.getEmail())) {
                currUser.setEmail(requestDTO.getEmail());
                currUser.setAccountVerified(false);

                updated = true;
            } else {
                throw new UserAlreadyExistException("User already exist with email: " + requestDTO.getEmail());
            }
        }

        if (!Objects.equals(currUser.getUsername(), requestDTO.getUsername())) {
            if (!userRepo.existsByUsername(requestDTO.getUsername())) {
                currUser.setUsername(requestDTO.getUsername());

                updated = true;
            } else {
                throw new UserAlreadyExistException("User already exist with username: " + requestDTO.getUsername());
            }
        }

        if (updated) {
            currUser.markUpdatedAt();
            userRepo.save(currUser);
        }
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
