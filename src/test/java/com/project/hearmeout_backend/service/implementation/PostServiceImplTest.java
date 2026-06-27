package com.project.hearmeout_backend.service.implementation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.common_lib.exception.TagNotFoundException;
import com.project.hearmeout_backend.interaction_service.repository.CommentRepository;
import com.project.hearmeout_backend.interaction_service.repository.VoteRepository;
import com.project.hearmeout_backend.interaction_service.service.implementation.CommentServiceImpl;
import com.project.hearmeout_backend.post_service.dto.request.QuestionSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.model.Post;
import com.project.hearmeout_backend.post_service.model.Tag;
import com.project.hearmeout_backend.post_service.model.enums.PostStatus;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.post_service.repository.PostRepository;
import com.project.hearmeout_backend.post_service.repository.TagRepository;
import com.project.hearmeout_backend.post_service.service.implementation.PostServiceImpl;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PostServiceImplTest {

  @Mock private PostRepository postRepo;
  @Mock private TagRepository tagRepo;
  @Mock private UserServiceImpl userServiceImpl;
  @Mock private VoteRepository voteRepo;
  @Mock private CommentRepository commentRepo;
  @Mock private UserRepository userRepo;
  @Mock private CommentServiceImpl commentServiceImpl;

  @InjectMocks private PostServiceImpl postService;

  private User author;
  private Tag tag1;
  private Tag tag2;
  private Tag tag3;

  @BeforeEach
  void setUp() {
    author = User.builder().username("testUser").reputation(10).build();
    author.setId(1L);

    tag1 = Tag.builder().name("java").description("Java lang").usageCount(5L).build();
    tag1.setId(1L);

    tag2 = Tag.builder().name("spring").description("Spring framework").usageCount(3L).build();
    tag2.setId(2L);

    tag3 = Tag.builder().name("kotlin").description("Kotlin lang").usageCount(0L).build();
    tag3.setId(3L);
  }

  // ===================== postNewQuestion tests =====================

  @Test
  void postNewQuestion_IncrementsTagUsageCount() {
    // Arrange
    QuestionSubmitRequestDTO dto = new QuestionSubmitRequestDTO();
    dto.setTitle("How to use Spring Boot with Java for building REST APIs effectively?");
    dto.setBody(
        "I am trying to build a REST API using Spring Boot. I need help with configuring controllers, services, and repositories. Can someone provide a detailed guide on how to set up the project structure and implement CRUD operations?");
    dto.setTagIds(List.of(1L, 2L));

    when(userServiceImpl.checkAndGetUserByUserId(1L)).thenReturn(author);
    when(tagRepo.findAllById(List.of(1L, 2L))).thenReturn(List.of(tag1, tag2));

    // Act
    postService.postNewQuestion(dto, 1L);

    // Assert
    verify(tagRepo).incrementUsageCount(List.of(1L, 2L));
    verify(postRepo).save(any(Post.class));
  }

  @Test
  void postNewQuestion_TagNotFound_DoesNotIncrement() {
    // Arrange
    QuestionSubmitRequestDTO dto = new QuestionSubmitRequestDTO();
    dto.setTitle("How to use Spring Boot with Java for building REST APIs effectively?");
    dto.setBody(
        "I am trying to build a REST API using Spring Boot. I need help with configuring controllers, services, and repositories. Can someone provide a detailed guide on how to set up the project structure and implement CRUD operations?");
    dto.setTagIds(List.of(1L, 99L));

    when(userServiceImpl.checkAndGetUserByUserId(1L)).thenReturn(author);
    when(tagRepo.findAllById(List.of(1L, 99L))).thenReturn(List.of(tag1));

    // Act & Assert
    assertThrows(TagNotFoundException.class, () -> postService.postNewQuestion(dto, 1L));

    verify(tagRepo, never()).incrementUsageCount(any());
  }

  // ===================== updateQuestion tests =====================

  @Test
  void updateQuestion_SameTags_NoCountChange() {
    // Arrange
    Post question =
        Post.builder()
            .postType(PostType.QUESTION)
            .author(author)
            .status(PostStatus.UNANSWERED)
            .tags(new ArrayList<>(List.of(tag1, tag2)))
            .build();
    question.setId(10L);

    QuestionSubmitRequestDTO dto = new QuestionSubmitRequestDTO();
    dto.setTitle("Updated title for the question about Spring Boot and Java REST APIs?");
    dto.setBody(
        "Updated body content with more details about building REST APIs. I need help with configuring controllers, services, and repositories. Can someone provide a detailed guide on how to set up the project structure and implement CRUD operations?");
    dto.setTagIds(List.of(1L, 2L));

    when(postRepo.findById(10L)).thenReturn(java.util.Optional.of(question));
    when(tagRepo.findAllById(List.of(1L, 2L))).thenReturn(List.of(tag1, tag2));

    // Act
    postService.updateQuestion(10L, dto, 1L);

    // Assert — no increment/decrement since tags haven't changed
    verify(tagRepo, never()).incrementUsageCount(any());
    verify(tagRepo, never()).decrementUsageCount(any());
    verify(postRepo).save(question);
  }

  @Test
  void updateQuestion_TagsChanged_IncrementsAndDecrements() {
    // Arrange — question currently has tag1 and tag2, update changes to tag2 and tag3
    Post question =
        Post.builder()
            .postType(PostType.QUESTION)
            .author(author)
            .status(PostStatus.UNANSWERED)
            .tags(new ArrayList<>(List.of(tag1, tag2)))
            .build();
    question.setId(10L);

    QuestionSubmitRequestDTO dto = new QuestionSubmitRequestDTO();
    dto.setTitle("Updated title for the question about Spring Boot and Kotlin REST APIs?");
    dto.setBody(
        "Updated body content about building REST APIs with Kotlin. I need help with configuring controllers, services, and repositories. Can someone provide a detailed guide on how to set up the project structure and implement CRUD operations?");
    dto.setTagIds(List.of(2L, 3L));

    when(postRepo.findById(10L)).thenReturn(java.util.Optional.of(question));
    when(tagRepo.findAllById(List.of(2L, 3L))).thenReturn(List.of(tag2, tag3));

    // Act
    postService.updateQuestion(10L, dto, 1L);

    // Assert — tag1 removed (decrement), tag3 added (increment), tag2 unchanged
    verify(tagRepo).decrementUsageCount(List.of(1L));
    verify(tagRepo).incrementUsageCount(List.of(3L));
    verify(postRepo).save(question);
  }

  @Test
  void updateQuestion_AllTagsReplaced() {
    // Arrange — question has tag1, update changes to tag3
    Post question =
        Post.builder()
            .postType(PostType.QUESTION)
            .author(author)
            .status(PostStatus.UNANSWERED)
            .tags(new ArrayList<>(List.of(tag1)))
            .build();
    question.setId(10L);

    QuestionSubmitRequestDTO dto = new QuestionSubmitRequestDTO();
    dto.setTitle("Updated title for a completely different question about Kotlin programming?");
    dto.setBody(
        "Completely rewritten body about Kotlin programming. I need help with configuring controllers, services, and repositories. Can someone provide a detailed guide on how to set up the project structure and implement CRUD operations?");
    dto.setTagIds(List.of(3L));

    when(postRepo.findById(10L)).thenReturn(java.util.Optional.of(question));
    when(tagRepo.findAllById(List.of(3L))).thenReturn(List.of(tag3));

    // Act
    postService.updateQuestion(10L, dto, 1L);

    // Assert
    verify(tagRepo).decrementUsageCount(List.of(1L));
    verify(tagRepo).incrementUsageCount(List.of(3L));
  }

  @Test
  void updateQuestion_ClosedQuestion_DoesNotModifyCounts() {
    // Arrange
    Post question =
        Post.builder()
            .postType(PostType.QUESTION)
            .author(author)
            .status(PostStatus.CLOSED)
            .tags(new ArrayList<>(List.of(tag1)))
            .build();
    question.setId(10L);

    QuestionSubmitRequestDTO dto = new QuestionSubmitRequestDTO();
    dto.setTitle("Should not matter because the question is closed and cannot be modified");
    dto.setBody(
        "This body should also not matter because the question is already resolved and closed for further modifications. No changes should be applied to this question.");
    dto.setTagIds(List.of(3L));

    when(postRepo.findById(10L)).thenReturn(java.util.Optional.of(question));

    // Act & Assert
    assertThrows(InvalidOperationException.class, () -> postService.updateQuestion(10L, dto, 1L));

    verify(tagRepo, never()).incrementUsageCount(any());
    verify(tagRepo, never()).decrementUsageCount(any());
  }

  @Test
  void updateQuestion_NotAuthor_DoesNotModifyCounts() {
    // Arrange
    User otherUser = User.builder().username("other").reputation(5).build();
    otherUser.setId(2L);

    Post question =
        Post.builder()
            .postType(PostType.QUESTION)
            .author(author)
            .status(PostStatus.UNANSWERED)
            .tags(new ArrayList<>(List.of(tag1)))
            .build();
    question.setId(10L);

    QuestionSubmitRequestDTO dto = new QuestionSubmitRequestDTO();
    dto.setTitle("Should not matter because this user is not the author of the question");
    dto.setBody(
        "This body should also not matter because the user trying to modify this question is not the original author. Only the original author can modify their own questions.");
    dto.setTagIds(List.of(3L));

    when(postRepo.findById(10L)).thenReturn(java.util.Optional.of(question));

    // Act & Assert
    assertThrows(InvalidOperationException.class, () -> postService.updateQuestion(10L, dto, 2L));

    verify(tagRepo, never()).incrementUsageCount(any());
    verify(tagRepo, never()).decrementUsageCount(any());
  }

  // ===================== deleteQuestion tests =====================

  @Test
  void deleteQuestion_DecrementsTagUsageCount() {
    // Arrange
    Post question =
        Post.builder()
            .postType(PostType.QUESTION)
            .author(author)
            .status(PostStatus.UNANSWERED)
            .tags(new ArrayList<>(List.of(tag1, tag2)))
            .build();
    question.setId(10L);

    when(postRepo.findById(10L)).thenReturn(java.util.Optional.of(question));
    when(userServiceImpl.checkAndGetUserByUserId(1L)).thenReturn(author);
    when(voteRepo.findAllByPostId(10L)).thenReturn(List.of());

    // Act
    postService.deleteQuestion(10L, 1L);

    // Assert
    verify(tagRepo).decrementUsageCount(List.of(1L, 2L));
    verify(postRepo).delete(question);
  }

  @Test
  void deleteQuestion_NoTags_SkipsDecrement() {
    // Arrange
    Post question =
        Post.builder()
            .postType(PostType.QUESTION)
            .author(author)
            .status(PostStatus.UNANSWERED)
            .tags(new ArrayList<>())
            .build();
    question.setId(10L);

    when(postRepo.findById(10L)).thenReturn(java.util.Optional.of(question));
    when(userServiceImpl.checkAndGetUserByUserId(1L)).thenReturn(author);
    when(voteRepo.findAllByPostId(10L)).thenReturn(List.of());

    // Act
    postService.deleteQuestion(10L, 1L);

    // Assert
    verify(tagRepo, never()).decrementUsageCount(any());
    verify(postRepo).delete(question);
  }

  @Test
  void deleteQuestion_ClosedQuestion_DoesNotDecrementCounts() {
    // Arrange
    Post question =
        Post.builder()
            .postType(PostType.QUESTION)
            .author(author)
            .status(PostStatus.CLOSED)
            .tags(new ArrayList<>(List.of(tag1)))
            .build();
    question.setId(10L);

    when(postRepo.findById(10L)).thenReturn(java.util.Optional.of(question));

    // Act & Assert
    assertThrows(InvalidOperationException.class, () -> postService.deleteQuestion(10L, 1L));

    verify(tagRepo, never()).decrementUsageCount(any());
    verify(postRepo, never()).delete(any(Post.class));
  }
}
