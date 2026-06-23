package com.project.hearmeout_backend.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.project.hearmeout_backend.common_lib.exception.UserAlreadyExistException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.interaction_service.model.Comment;
import com.project.hearmeout_backend.interaction_service.repository.CommentRepository;
import com.project.hearmeout_backend.post_service.model.Post;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.post_service.repository.PostRepository;
import com.project.hearmeout_backend.user_service.dto.request.UserProfileModificationRequestDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserAnswerResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserCommentResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserProfileResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserQuestionResponseDTO;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

  @Mock private UserRepository userRepo;

  @Mock private PostRepository postRepo;

  @Mock private CommentRepository commentRepo;

  @Spy @InjectMocks private UserServiceImpl userService;

  private List<User> userList;
  private List<Post> postList;
  private List<Comment> commentList;

  @BeforeEach
  public void setUp() {
    // USERS
    User user1 =
        User.builder()
            .username("test1")
            .email("test1@gmail.com")
            .emailUpdatedAt(LocalDate.now().minusDays(30))
            .usernameUpdatedAt(LocalDate.now().minusDays(30))
            .build();
    user1.setId(1L);

    User user2 =
        User.builder()
            .username("test2")
            .email("test2@gmail.com")
            .emailUpdatedAt(LocalDate.now().minusDays(30))
            .usernameUpdatedAt(LocalDate.now().minusDays(30))
            .build();
    user2.setId(2L);

    User user3 =
        User.builder()
            .username("test3")
            .email("test3@gmail.com")
            .emailUpdatedAt(LocalDate.now().minusDays(30))
            .usernameUpdatedAt(LocalDate.now().minusDays(30))
            .build();
    user3.setId(3L);

    userList = List.of(user1, user2, user3);

    // POSTS
    Post post1 = Post.builder().title("Answer 1").postType(PostType.ANSWER).author(user2).build();
    post1.setId(1L);

    Post post2 =
        Post.builder().title("Question 1").postType(PostType.QUESTION).author(user3).build();
    post2.setId(2L);

    Post post3 =
        Post.builder().title("Question 2").postType(PostType.QUESTION).author(user1).build();
    post3.setId(3L);

    postList = List.of(post1, post2, post3);

    // COMMENTS
    Comment comment1 = Comment.builder().body("Comment 1").author(user2).post(post3).build();
    comment1.setId(1L);

    Comment comment2 = Comment.builder().body("Comment 2").author(user1).post(post2).build();
    comment2.setId(2L);

    commentList = List.of(comment1, comment2);

    // Relation
    post1.setParent(post2);
  }

  @ParameterizedTest
  @CsvSource({"test5, 1"})
  public void getUserProfile_UnregisteredUser(String username, Long currUserId)
      throws UserNotFoundException {
    // Arrange
    UserProfileResponseDTO userProfile =
        userList.stream()
            .filter(user -> Objects.equals(user.getUsername(), username))
            .findFirst()
            .map(
                user ->
                    UserProfileResponseDTO.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .isOperable(Objects.equals(currUserId, user.getId()))
                        .build())
            .orElse(null);

    when(userRepo.getUserProfileByUsername(username)).thenReturn(Optional.ofNullable(userProfile));

    // Act and Assert
    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class, () -> userService.getUserProfile(username, currUserId));

    assertEquals("User not found with username: " + username, exception.getMessage());
  }

  @ParameterizedTest
  @CsvSource({"test1, 1", "test2, 4", "test3, 2"})
  public void getUserProfile(String username, Long currUserId) {
    // Arrange
    UserProfileResponseDTO userProfile =
        userList.stream()
            .filter(user -> Objects.equals(user.getUsername(), username))
            .findFirst()
            .map(
                user ->
                    UserProfileResponseDTO.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .isOperable(Objects.equals(currUserId, user.getId()))
                        .build())
            .orElse(null);

    when(userRepo.getUserProfileByUsername(username)).thenReturn(Optional.ofNullable(userProfile));

    // Act
    UserProfileResponseDTO result = userService.getUserProfile(username, currUserId);

    // Assert
    assertEquals(userProfile.getUsername(), result.getUsername());

    assertEquals(userProfile.isOperable(), result.isOperable());

    verify(userRepo).getUserProfileByUsername(username);
  }

  @ParameterizedTest
  @ValueSource(strings = {"test5"})
  public void checkUserExistenceByUsername_UnregisteredUser(String username) {
    // Arrange
    User userProfile =
        userList.stream()
            .filter(user -> Objects.equals(user.getUsername(), username))
            .findFirst()
            .orElse(null);

    when(userRepo.findByUsername(username)).thenReturn(Optional.ofNullable(userProfile));

    // Act and Assert
    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class, () -> userService.checkAndGetUserByUsername(username));

    assertEquals("User not found with username: " + username, exception.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"5"})
  public void checkUserExistenceByUserId_UnregisteredUser(Long userId) {
    // Arrange
    User userProfile =
        userList.stream()
            .filter(user -> Objects.equals(user.getId(), userId))
            .findFirst()
            .orElse(null);

    when(userRepo.findById(userId)).thenReturn(Optional.ofNullable(userProfile));

    // Act and Assert
    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class, () -> userService.checkAndGetUserByUserId(userId));

    assertEquals("User not found with id: " + userId, exception.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"test5@gmail.com"})
  public void checkUserExistenceByEmail_UnregisteredUser(String email) {
    // Arrange
    User userProfile =
        userList.stream()
            .filter(user -> Objects.equals(user.getEmail(), email))
            .findFirst()
            .orElse(null);

    when(userRepo.findByEmail(email)).thenReturn(Optional.ofNullable(userProfile));

    // Act and Assert
    UserNotFoundException exception =
        assertThrows(UserNotFoundException.class, () -> userService.checkAndGetUserByEmail(email));

    assertEquals("User not found with email: " + email, exception.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"test1", "test2", "test3"})
  public void getUserQuestions(String username) {
    // Arrange
    User userProfile =
        userList.stream()
            .filter(user -> Objects.equals(user.getUsername(), username))
            .findFirst()
            .orElse(null);

    doReturn(userProfile).when(userService).checkAndGetUserByUsername(username);

    List<UserQuestionResponseDTO> questionResponse =
        postList.stream()
            .filter(
                post ->
                    Objects.equals(post.getPostType(), PostType.QUESTION)
                        && Objects.equals(post.getAuthor().getUsername(), username))
            .map(
                question ->
                    UserQuestionResponseDTO.builder()
                        .navigationPostId(question.getId())
                        .title(question.getTitle())
                        .build())
            .toList();

    when(postRepo.findUserQuestionByUsername(username, PostType.QUESTION))
        .thenReturn(questionResponse);

    // Act
    List<UserQuestionResponseDTO> result = userService.getUserQuestions(username);

    // Assert
    assertEquals(questionResponse, result);
  }

  @ParameterizedTest
  @ValueSource(strings = {"test1", "test2", "test3"})
  public void getUserAnswers(String username) {
    // Arrange
    User userProfile =
        userList.stream()
            .filter(user -> Objects.equals(user.getUsername(), username))
            .findFirst()
            .orElse(null);

    doReturn(userProfile).when(userService).checkAndGetUserByUsername(username);

    List<UserAnswerResponseDTO> answerResponse =
        postList.stream()
            .filter(
                post ->
                    Objects.equals(post.getPostType(), PostType.ANSWER)
                        && Objects.equals(post.getAuthor().getUsername(), username))
            .map(
                answer ->
                    UserAnswerResponseDTO.builder()
                        .navigationPostId(answer.getParent().getId())
                        .build())
            .toList();

    when(postRepo.findUserAnswerByUsername(username, PostType.ANSWER)).thenReturn(answerResponse);

    // Act
    List<UserAnswerResponseDTO> result = userService.getUserAnswers(username);

    // Assert
    assertEquals(answerResponse, result);
  }

  @ParameterizedTest
  @ValueSource(strings = {"test1", "test2", "test3"})
  public void getUserComments(String username) {
    // Arrange
    User userProfile =
        userList.stream()
            .filter(user -> Objects.equals(user.getUsername(), username))
            .findFirst()
            .orElse(null);

    doReturn(userProfile).when(userService).checkAndGetUserByUsername(username);

    List<UserCommentResponseDTO> commentResponse =
        commentList.stream()
            .filter(comment -> Objects.equals(comment.getAuthor().getUsername(), username))
            .map(
                comment ->
                    UserCommentResponseDTO.builder()
                        .navigationPostId(
                            comment.getPost().getParent() == null
                                ? comment.getPost().getId()
                                : comment.getPost().getParent().getId())
                        .build())
            .toList();

    when(commentRepo.findUserCommentsByUsername(username)).thenReturn(commentResponse);

    // Act
    List<UserCommentResponseDTO> result = userService.getUserComments(username);

    // Assert
    assertEquals(commentResponse, result);
  }

  @ParameterizedTest
  @ValueSource(longs = {1L, 2L})
  public void terminateUserAccount(Long userId) {
    // Arrange
    User userProfile =
        userList.stream()
            .filter(user -> Objects.equals(user.getId(), userId))
            .findFirst()
            .orElse(null);

    doReturn(userProfile).when(userService).checkAndGetUserByUserId(userId);

    // Act
    userService.terminateUserAccount(userId);

    // Assert
    verify(userRepo).delete(userProfile);
  }

  @ParameterizedTest
  @ValueSource(longs = {1, 2})
  public void updateUserDetails_DuplicateUsername(Long currUserId) {
    // Arrange
    UserProfileModificationRequestDTO requestDTO =
        new UserProfileModificationRequestDTO("test3", "test4@gmail.com");

    User userProfile =
        userList.stream()
            .filter(user -> Objects.equals(user.getId(), currUserId))
            .findFirst()
            .orElse(null);

    doReturn(userProfile).when(userService).checkAndGetUserByUserId(currUserId);

    doReturn(
            userList.stream()
                .anyMatch(
                    user ->
                        !Objects.equals(user.getId(), currUserId)
                            && Objects.equals(user.getEmail(), requestDTO.getEmail())))
        .when(userRepo)
        .existsByEmail(requestDTO.getEmail());

    doReturn(
            userList.stream()
                .anyMatch(
                    user ->
                        !Objects.equals(user.getId(), currUserId)
                            && Objects.equals(user.getUsername(), requestDTO.getUsername())))
        .when(userRepo)
        .existsByUsername(requestDTO.getUsername());

    // Act
    UserAlreadyExistException exception =
        assertThrows(
            UserAlreadyExistException.class,
            () -> userService.updateUserDetails(requestDTO, currUserId));

    // Assert
    assertEquals(
        "User already exist with username: " + requestDTO.getUsername(), exception.getMessage());
  }

  @ParameterizedTest
  @ValueSource(longs = {1, 2})
  public void updateUserDetails_DuplicateEmail(Long currUserId) {
    // Arrange
    UserProfileModificationRequestDTO requestDTO =
        new UserProfileModificationRequestDTO("test4", "test3@gmail.com");

    User userProfile =
        userList.stream()
            .filter(user -> Objects.equals(user.getId(), currUserId))
            .findFirst()
            .orElse(null);

    doReturn(userProfile).when(userService).checkAndGetUserByUserId(currUserId);

    doReturn(
            userList.stream()
                .anyMatch(
                    user ->
                        !Objects.equals(user.getId(), currUserId)
                            && Objects.equals(user.getEmail(), requestDTO.getEmail())))
        .when(userRepo)
        .existsByEmail(requestDTO.getEmail());

    // Act
    UserAlreadyExistException exception =
        assertThrows(
            UserAlreadyExistException.class,
            () -> userService.updateUserDetails(requestDTO, currUserId));

    // Assert
    assertEquals("User already exist with email: " + requestDTO.getEmail(), exception.getMessage());
  }

  @ParameterizedTest
  @ValueSource(longs = {1, 2})
  public void updateUserDetails_NoUpdate(Long currUserId) {
    // Arrange
    UserProfileModificationRequestDTO requestDTO =
        new UserProfileModificationRequestDTO(
            "test" + currUserId, "test" + currUserId + "@gmail.com");

    User userProfile =
        userList.stream()
            .filter(user -> Objects.equals(user.getId(), currUserId))
            .findFirst()
            .orElse(null);

    doReturn(userProfile).when(userService).checkAndGetUserByUserId(currUserId);

    // Act
    userService.updateUserDetails(requestDTO, currUserId);

    // Assert
    verify(userRepo, never()).save(userProfile);
  }

  @ParameterizedTest
  @ValueSource(longs = {1, 2})
  public void updateUserDetails_SuccessfulUpdate(Long currUserId) {
    // Arrange
    UserProfileModificationRequestDTO requestDTO =
        new UserProfileModificationRequestDTO("test4", "test4@gmail.com");

    User userProfile =
        userList.stream()
            .filter(user -> Objects.equals(user.getId(), currUserId))
            .findFirst()
            .orElse(null);

    doReturn(userProfile).when(userService).checkAndGetUserByUserId(currUserId);

    doReturn(
            userList.stream()
                .anyMatch(user -> Objects.equals(user.getEmail(), requestDTO.getEmail())))
        .when(userRepo)
        .existsByEmail(requestDTO.getEmail());

    doReturn(
            userList.stream()
                .anyMatch(user -> Objects.equals(user.getUsername(), requestDTO.getUsername())))
        .when(userRepo)
        .existsByUsername(requestDTO.getUsername());

    // Act
    userService.updateUserDetails(requestDTO, currUserId);

    // Assert
    verify(userRepo).save(userProfile);
  }
}
