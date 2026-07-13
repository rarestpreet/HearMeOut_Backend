package com.project.hearmeout_backend.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.interaction_service.dto.request.CommentRequestDTO;
import com.project.hearmeout_backend.interaction_service.model.Comment;
import com.project.hearmeout_backend.interaction_service.repository.CommentRepository;
import com.project.hearmeout_backend.interaction_service.service.implementation.CommentServiceImpl;
import com.project.hearmeout_backend.post_service.model.ErrorReport;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.post_service.repository.ErrorReportRepository;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

  @Mock private CommentRepository commentRepo;
  @Mock private UserServiceImpl userServiceImpl;
  @Mock private UserRepository userRepo;
  @Mock private ErrorReportRepository errorReportRepo;

  @InjectMocks private CommentServiceImpl commentService;

  private User author;
  private ErrorReport errorReport;
  private Comment comment;
  private CommentRequestDTO commentRequestDTO;
  private UUID authorId;
  private UUID reportId;
  private UUID commentId;

  @BeforeEach
  void setUp() {
    authorId = UUID.randomUUID();
    author = User.builder().username("authorUser").reputation(10).build();
    author.setId(authorId);

    reportId = UUID.randomUUID();
    errorReport = ErrorReport.builder().build();
    errorReport.setId(reportId);

    commentId = UUID.randomUUID();
    comment = Comment.builder().author(author).body("Old Body").parentId(reportId).parentType(PostType.ERROR_REPORT).build();
    comment.setId(commentId);

    commentRequestDTO = new CommentRequestDTO();
    commentRequestDTO.setParentId(reportId);
    commentRequestDTO.setParentType(PostType.ERROR_REPORT);
    commentRequestDTO.setBody("This is a comment");
  }

  @Test
  void createNewComment_ValidParent() {
    when(userServiceImpl.checkAndGetUserByUserId(authorId)).thenReturn(author);
    when(errorReportRepo.existsById(reportId)).thenReturn(true);

    commentService.createNewComment(commentRequestDTO, authorId);

    verify(commentRepo).save(any(Comment.class));
    verify(userRepo).save(author);
  }

  @Test
  void createNewComment_InvalidParent_ThrowsException() {
    when(userServiceImpl.checkAndGetUserByUserId(authorId)).thenReturn(author);
    when(errorReportRepo.existsById(reportId)).thenReturn(false);

    InvalidOperationException exception = assertThrows(InvalidOperationException.class, () -> commentService.createNewComment(commentRequestDTO, authorId));

    assertEquals("Error report not found: " + reportId, exception.getMessage());
    verify(commentRepo, never()).save(any(Comment.class));
  }

  @Test
  void updateCommentBody_ValidAuthor() {
    String newBody = "New Body";

    when(commentRepo.findById(commentId)).thenReturn(Optional.of(comment));

    commentService.updateCommentBody(commentId, newBody, authorId);

    assertEquals(newBody, comment.getBody());
    verify(commentRepo).save(comment);
  }

  @Test
  void updateCommentBody_NotAuthor_ThrowsException() {
    UUID otherUserId = UUID.randomUUID();
    String newBody = "New Body";

    when(commentRepo.findById(commentId)).thenReturn(Optional.of(comment));

    InvalidOperationException exception = assertThrows(InvalidOperationException.class, () -> commentService.updateCommentBody(commentId, newBody, otherUserId));

    assertEquals("Operation only allowed for account owner", exception.getMessage());
    verify(commentRepo, never()).save(any(Comment.class));
  }

  @Test
  void removeComment_ValidAuthor() {
    when(commentRepo.findById(commentId)).thenReturn(Optional.of(comment));
    when(userServiceImpl.checkAndGetUserByUserId(authorId)).thenReturn(author);

    commentService.removeComment(commentId, authorId);

    verify(commentRepo).delete(comment);
    verify(userRepo).save(author);
  }
}
