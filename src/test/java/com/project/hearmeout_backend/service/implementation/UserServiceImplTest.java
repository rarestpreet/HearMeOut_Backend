package com.project.hearmeout_backend.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.common_lib.exception.UserAlreadyExistException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.interaction_service.repository.CommentRepository;
import com.project.hearmeout_backend.post_service.repository.ErrorReportRepository;
import com.project.hearmeout_backend.post_service.repository.SolutionRepository;
import com.project.hearmeout_backend.user_service.dto.request.UserProfileModificationRequestDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserAnswerResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserCommentResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserErrorReportResponseDTO;
import com.project.hearmeout_backend.user_service.model.Profession;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.ProfessionRepository;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

  @Mock private UserRepository userRepo;
  @Mock private ErrorReportRepository errorReportRepo;
  @Mock private SolutionRepository solutionRepo;
  @Mock private CommentRepository commentRepo;
  @Mock private ProfessionRepository professionRepo;

  @Spy @InjectMocks private UserServiceImpl userService;

  private List<User> userList;
  private UUID user1Id, user2Id, user3Id;

  @BeforeEach
  public void setUp() {
    user1Id = UUID.randomUUID();
    user2Id = UUID.randomUUID();
    user3Id = UUID.randomUUID();

    User user1 =
        User.builder()
            .username("test1")
            .email("test1@gmail.com")
            .emailUpdatedAt(LocalDateTime.now().minusDays(30))
            .usernameUpdatedAt(LocalDateTime.now().minusDays(30))
            .build();
    user1.setId(user1Id);

    User user2 =
        User.builder()
            .username("test2")
            .email("test2@gmail.com")
            .emailUpdatedAt(LocalDateTime.now().minusDays(30))
            .usernameUpdatedAt(LocalDateTime.now().minusDays(30))
            .build();
    user2.setId(user2Id);

    User user3 =
        User.builder()
            .username("test3")
            .email("test3@gmail.com")
            .emailUpdatedAt(LocalDateTime.now().minusDays(30))
            .usernameUpdatedAt(LocalDateTime.now().minusDays(30))
            .build();
    user3.setId(user3Id);

    userList = List.of(user1, user2, user3);
  }

  @Test
  public void getUserProfile_UnregisteredUser() throws UserNotFoundException {
    String username = "test5";
    when(userRepo.getUserProfileByUsername(username)).thenReturn(Optional.empty());

    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class, () -> userService.getUserProfile(username, user1Id));
    assertEquals("User not found with username: " + username, exception.getMessage());
  }

  @Test
  public void checkUserExistenceByUserId_UnregisteredUser() {
    UUID randomId = UUID.randomUUID();
    when(userRepo.findById(randomId)).thenReturn(Optional.empty());

    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class, () -> userService.checkAndGetUserByUserId(randomId));
    assertEquals("User not found with id: " + randomId, exception.getMessage());
  }

  @Test
  public void updateUserDetails_DuplicateUsername() {
    // email changes from test1@gmail.com → test4@gmail.com, so existsByEmail is checked first
    UserProfileModificationRequestDTO requestDTO =
        new UserProfileModificationRequestDTO("test3", "test4@gmail.com", "John Doe", "Bio", "Dev");
    User userProfile = userList.get(0);

    doReturn(userProfile).when(userService).checkAndGetUserByUserId(user1Id);
    doReturn(false).when(userRepo).existsByEmail(requestDTO.getEmail()); // email check passes
    doReturn(true).when(userRepo).existsByUsername(requestDTO.getUsername()); // username is taken

    UserAlreadyExistException exception =
        assertThrows(
            UserAlreadyExistException.class,
            () -> userService.updateUserDetails(requestDTO, user1Id));
    assertEquals(
        "User already exist with username: " + requestDTO.getUsername(), exception.getMessage());
  }

  @Test
  public void updateUserDetails_SuccessfulUpdate() {
    UserProfileModificationRequestDTO requestDTO =
        new UserProfileModificationRequestDTO("test4", "test4@gmail.com", "John Doe", "Bio", "Dev");
    User userProfile = userList.get(0);
    Profession mockProfession = Profession.builder().name("DEV").build();

    doReturn(userProfile).when(userService).checkAndGetUserByUserId(user1Id);
    doReturn(false).when(userRepo).existsByEmail(requestDTO.getEmail());
    doReturn(false).when(userRepo).existsByUsername(requestDTO.getUsername());
    // profession="Dev" triggers findByNameIgnoreCase (empty) then save
    when(professionRepo.findByNameIgnoreCase("Dev")).thenReturn(Optional.empty());
    when(professionRepo.save(any())).thenReturn(mockProfession);

    userService.updateUserDetails(requestDTO, user1Id);

    verify(userRepo).save(userProfile);
    verify(professionRepo).save(any());
  }

  @Test
  public void getUserErrorReports_ValidUsername() {
    String username = "test1";
    User userProfile = userList.get(0);
    doReturn(userProfile).when(userService).checkAndGetUserByUsername(username);

    UserErrorReportResponseDTO responseDTO =
        UserErrorReportResponseDTO.builder().title("Title").navigationId(UUID.randomUUID()).build();
    Page<UserErrorReportResponseDTO> page =
        new PageImpl<>(List.of(responseDTO), PageRequest.of(0, 5), 1);

    when(errorReportRepo.findUserErrorReportsByUsername(eq(username), any(Pageable.class)))
        .thenReturn(page);

    PagedResponse<UserErrorReportResponseDTO> result =
        userService.getUserErrorReports(username, 5, 0);

    assertEquals(1, result.getData().size());
    assertEquals("Title", result.getData().get(0).getTitle());
  }

  @Test
  public void getUserSolutions_ValidUsername() {
    String username = "test2";
    User userProfile = userList.get(1);
    doReturn(userProfile).when(userService).checkAndGetUserByUsername(username);

    UserAnswerResponseDTO responseDTO =
        UserAnswerResponseDTO.builder().navigationId(UUID.randomUUID()).build();
    Page<UserAnswerResponseDTO> page =
        new PageImpl<>(List.of(responseDTO), PageRequest.of(0, 5), 1);

    when(solutionRepo.findUserSolutionsByUsername(eq(username), any(Pageable.class)))
        .thenReturn(page);

    PagedResponse<UserAnswerResponseDTO> result = userService.getUserSolutions(username, 5, 0);

    assertEquals(1, result.getData().size());
  }

  @Test
  public void getUserComments_ValidUsername() {
    String username = "test3";
    User userProfile = userList.get(2);
    doReturn(userProfile).when(userService).checkAndGetUserByUsername(username);

    UserCommentResponseDTO responseDTO =
        UserCommentResponseDTO.builder().parentId(UUID.randomUUID()).build();
    Page<UserCommentResponseDTO> page =
        new PageImpl<>(List.of(responseDTO), PageRequest.of(0, 5), 1);

    when(commentRepo.findUserCommentsByUsername(eq(username), any(Pageable.class)))
        .thenReturn(page);

    PagedResponse<UserCommentResponseDTO> result = userService.getUserComments(username, 5, 0);

    assertEquals(1, result.getData().size());
  }
}
