package com.project.hearmeout_backend.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.interaction_service.dto.request.VoteRequestDTO;
import com.project.hearmeout_backend.interaction_service.model.Vote;
import com.project.hearmeout_backend.interaction_service.model.enums.VoteType;
import com.project.hearmeout_backend.interaction_service.repository.VoteRepository;
import com.project.hearmeout_backend.interaction_service.service.implementation.VoteServiceImpl;
import com.project.hearmeout_backend.post_service.model.ErrorReport;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.post_service.repository.ErrorReportRepository;
import com.project.hearmeout_backend.post_service.repository.SolutionRepository;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import java.util.Optional;
import java.util.UUID;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VoteServiceImplTest {

  @Mock private VoteRepository voteRepo;
  @Mock private UserServiceImpl userServiceImpl;
  @Mock private UserRepository userRepo;
  @Mock private ErrorReportRepository errorReportRepo;
  @Mock private SolutionRepository solutionRepo;

  @InjectMocks private VoteServiceImpl voteService;

  private User currUser;
  private User author;
  private ErrorReport errorReport;
  private VoteRequestDTO voteRequestDTO;
  private UUID currUserId;
  private UUID authorId;
  private UUID reportId;

  @BeforeEach
  void setUp() {
    currUserId = UUID.randomUUID();
    currUser = User.builder().username("currUser").reputation(10).build();
    currUser.setId(currUserId);

    authorId = UUID.randomUUID();
    author = User.builder().username("author").reputation(20).build();
    author.setId(authorId);

    reportId = UUID.randomUUID();
    errorReport = ErrorReport.builder().build();
    errorReport.setAuthor(author);
    errorReport.setScore(5);
    errorReport.setId(reportId);

    voteRequestDTO = new VoteRequestDTO();
    voteRequestDTO.setParentId(reportId);
    voteRequestDTO.setParentType(PostType.ERROR_REPORT);
  }

  @Test
  void handleVote_SelfPost() {
    // Arrange
    errorReport.setAuthor(currUser);

    when(voteRepo.findByParentIdAndParentTypeAndUserId(reportId, PostType.ERROR_REPORT, currUserId)).thenReturn(Optional.empty());
    when(userServiceImpl.checkAndGetUserByUserId(currUserId)).thenReturn(currUser);
    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(errorReport));

    // Act & Assert
    InvalidOperationException exception =
        assertThrows(
            InvalidOperationException.class,
            () -> voteService.handleVote(voteRequestDTO, currUserId, VoteType.UPVOTE));

    assertEquals("Invalid action: you cannot vote your own posts.", exception.getMessage());
  }

  @Test
  void handleVote_NewUpvote() throws BadRequestException {
    // Arrange
    when(voteRepo.findByParentIdAndParentTypeAndUserId(reportId, PostType.ERROR_REPORT, currUserId)).thenReturn(Optional.empty());
    when(userServiceImpl.checkAndGetUserByUserId(currUserId)).thenReturn(currUser);
    when(userServiceImpl.checkAndGetUserByUserId(authorId)).thenReturn(author);
    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(errorReport));

    // Act
    voteService.handleVote(voteRequestDTO, currUserId, VoteType.UPVOTE);

    // Assert
    assertEquals(21, author.getReputation());
    assertEquals(6, errorReport.getScore());
    assertEquals(11, currUser.getReputation());

    verify(userRepo).save(author);
    verify(errorReportRepo).save(errorReport);
    verify(userRepo).save(currUser);

    ArgumentCaptor<Vote> voteCaptor = ArgumentCaptor.forClass(Vote.class);
    verify(voteRepo).save(voteCaptor.capture());
    Vote savedVote = voteCaptor.getValue();

    assertEquals(VoteType.UPVOTE, savedVote.getVoteType());
    assertEquals(currUser, savedVote.getUser());
    assertEquals(reportId, savedVote.getParentId());
  }

  @Test
  void handleVote_NewDownvote() throws BadRequestException {
    when(voteRepo.findByParentIdAndParentTypeAndUserId(reportId, PostType.ERROR_REPORT, currUserId)).thenReturn(Optional.empty());
    when(userServiceImpl.checkAndGetUserByUserId(currUserId)).thenReturn(currUser);
    when(userServiceImpl.checkAndGetUserByUserId(authorId)).thenReturn(author);
    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(errorReport));

    voteService.handleVote(voteRequestDTO, currUserId, VoteType.DOWNVOTE);

    assertEquals(19, author.getReputation());
    assertEquals(4, errorReport.getScore());
    assertEquals(11, currUser.getReputation());

    verify(userRepo).save(author);
    verify(errorReportRepo).save(errorReport);
    verify(userRepo).save(currUser);
    verify(voteRepo).save(any(Vote.class));
  }

  @Test
  void handleVote_ExistingVoteRemoved_Upvote() throws BadRequestException {
    Vote existingVote = Vote.builder().user(currUser).parentId(reportId).parentType(PostType.ERROR_REPORT).voteType(VoteType.UPVOTE).build();

    when(voteRepo.findByParentIdAndParentTypeAndUserId(reportId, PostType.ERROR_REPORT, currUserId)).thenReturn(Optional.of(existingVote));
    when(userServiceImpl.checkAndGetUserByUserId(currUserId)).thenReturn(currUser);
    when(userServiceImpl.checkAndGetUserByUserId(authorId)).thenReturn(author);
    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(errorReport));

    voteService.handleVote(voteRequestDTO, currUserId, VoteType.UPVOTE);

    assertEquals(19, author.getReputation());
    assertEquals(4, errorReport.getScore());
    assertEquals(9, currUser.getReputation());

    verify(voteRepo).deleteByParentIdAndParentTypeAndUserId(reportId, PostType.ERROR_REPORT, currUserId);
  }

  @Test
  void handleVote_ExistingVoteChanged_Upvote() throws BadRequestException {
    Vote existingVote = Vote.builder().user(currUser).parentId(reportId).parentType(PostType.ERROR_REPORT).voteType(VoteType.DOWNVOTE).build();

    when(voteRepo.findByParentIdAndParentTypeAndUserId(reportId, PostType.ERROR_REPORT, currUserId)).thenReturn(Optional.of(existingVote));
    when(userServiceImpl.checkAndGetUserByUserId(currUserId)).thenReturn(currUser);
    when(userServiceImpl.checkAndGetUserByUserId(authorId)).thenReturn(author);
    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(errorReport));

    voteService.handleVote(voteRequestDTO, currUserId, VoteType.UPVOTE);

    assertEquals(22, author.getReputation());
    assertEquals(7, errorReport.getScore());
    assertEquals(10, currUser.getReputation());

    assertEquals(VoteType.UPVOTE, existingVote.getVoteType());
    verify(voteRepo).save(existingVote);
  }
}
