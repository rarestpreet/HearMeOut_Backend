package com.project.hearmeout_backend.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.feed_service.dto.response.FeedErrorReportResponseDTO;
import com.project.hearmeout_backend.feed_service.dto.response.HomeUserProfileResponseDTO;
import com.project.hearmeout_backend.feed_service.service.implementation.HomeServiceImpl;
import com.project.hearmeout_backend.post_service.dto.response.ReportTagResponseDTO;
import com.project.hearmeout_backend.post_service.model.ErrorReport;
import com.project.hearmeout_backend.post_service.model.Tag;
import com.project.hearmeout_backend.post_service.repository.ErrorReportRepository;
import com.project.hearmeout_backend.post_service.repository.TagRepository;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class HomeServiceImplTest {

  @Mock private UserRepository userRepo;

  @Mock private ErrorReportRepository errorReportRepo;

  @Mock private TagRepository tagRepo;

  @InjectMocks private HomeServiceImpl homeService;

    private List<ErrorReport> postList;

    private UUID user1Id, user2Id, user3Id;

  @BeforeEach
  public void setUp() {
    user1Id = UUID.randomUUID();
    user2Id = UUID.randomUUID();
    user3Id = UUID.randomUUID();

    // USERS
    User user1 = User.builder().username("test1").email("test1@gmail.com").build();
    user1.setId(user1Id);

    User user2 = User.builder().username("test2").email("test2@gmail.com").build();
    user2.setId(user2Id);

    User user3 = User.builder().username("test3").email("test3@gmail.com").build();
    user3.setId(user3Id);

      List<User> userList = List.of(user1, user2, user3);

    // TAGS
    Tag tag1 = Tag.builder().name("Test1").build();
    tag1.setId(UUID.randomUUID());

    Tag tag2 = Tag.builder().name("Test2").build();
    tag2.setId(UUID.randomUUID());

    Tag tag3 = Tag.builder().name("Test3").build();
    tag3.setId(UUID.randomUUID());

    Tag tag4 = Tag.builder().name("Test4").build();
    tag4.setId(UUID.randomUUID());

      List<Tag> tagList = List.of(tag1, tag2, tag3, tag4);

    // POSTS
    ErrorReport post1 = ErrorReport.builder().title("Answer 1").build();
    post1.setId(UUID.randomUUID());
    post1.setAuthor(user2);

    ErrorReport post2 = ErrorReport.builder().title("Question 1").tags(List.of(tag1, tag2)).build();
    post2.setId(UUID.randomUUID());
    post2.setAuthor(user3);

    ErrorReport post3 = ErrorReport.builder().title("Question 2").tags(List.of(tag2, tag3)).build();
    post3.setId(UUID.randomUUID());
    post3.setAuthor(user1);

    ErrorReport post4 = ErrorReport.builder().title("Question 3").tags(List.of(tag1, tag3, tag4)).build();
    post4.setId(UUID.randomUUID());
    post4.setAuthor(user2);

    ErrorReport post5 = ErrorReport.builder().title("Answer 2").build();
    post5.setId(UUID.randomUUID());
    post5.setAuthor(user3);

    ErrorReport post6 = ErrorReport.builder().title("Question 4").tags(List.of(tag1, tag4)).build();
    post6.setId(UUID.randomUUID());
    post6.setAuthor(user1);

    ErrorReport post7 = ErrorReport.builder().title("Answer 3").build();
    post7.setId(UUID.randomUUID());
    post7.setAuthor(user2);

    ErrorReport post8 = ErrorReport.builder().title("Question 5").tags(List.of(tag2, tag4)).build();
    post8.setId(UUID.randomUUID());
    post8.setAuthor(user3);

    ErrorReport post9 = ErrorReport.builder().title("Question 6").tags(List.of(tag3, tag4)).build();
    post9.setId(UUID.randomUUID());
    post9.setAuthor(user1);

    postList = List.of(post1, post2, post3, post4, post5, post6, post7, post8, post9);
  }

  @Test
  public void getUserProfile_GuestUser() {
    // Act
    HomeUserProfileResponseDTO result = homeService.getUserProfile(null);

    // Assert
    assertNull(result.getUsername());
    assertNull(result.getUserNavigationId());
    assertNull(result.getRole());
  }

  @Test
  public void getUserProfile_InvalidUser() {
    // Arrange
    UUID invalidId = UUID.randomUUID();
    when(userRepo.getHomeUserProfileById(invalidId)).thenReturn(Optional.empty());

    // Act and Assert
    UserNotFoundException exception =
        assertThrows(UserNotFoundException.class, () -> homeService.getUserProfile(invalidId));

    assertEquals("User with id:  " + invalidId + " was not found", exception.getMessage());
  }

  @Test
  public void getUserProfile_AuthenticatedUser() {
    // Arrange
    UUID validId = user1Id;
    HomeUserProfileResponseDTO currUser =
        HomeUserProfileResponseDTO.builder()
            .userNavigationId(validId)
            .username("test1")
            .build();

    when(userRepo.getHomeUserProfileById(validId)).thenReturn(Optional.of(currUser));

    // Act
    HomeUserProfileResponseDTO result = homeService.getUserProfile(validId);

    // Assert
    assertEquals(currUser, result);
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 0, 1})
  public void generateFeed_GuestUser(int pageNum) {
    // Arrange
    pageNum = Math.max(0, pageNum);
    int limit = 10;
    int offset = pageNum * 10;
    Pageable pageable = PageRequest.of(pageNum, limit);

      List<FeedErrorReportResponseDTO> feedPost =
        postList
            .subList(Math.min(offset, postList.size()), Math.min(offset + 10, postList.size()))
            .stream()
            .map(
                post -> {
                  List<ReportTagResponseDTO> tags =
                      post.getTags() == null ? List.of() : post.getTags().stream()
                          .map(tag -> new ReportTagResponseDTO(tag.getId(), tag.getName(), tag.getDescription()))
                          .toList();

                  return FeedErrorReportResponseDTO.builder()
                      .navigationId(post.getId())
                      .title(post.getTitle())
                      .tags(tags)
                      .build();
                })
            .toList();

    feedPost.forEach(
        post ->
            when(tagRepo.findTagsByErrorReportId(post.getNavigationId()))
                .thenReturn(post.getTags()));

    Page<FeedErrorReportResponseDTO> pageImpl = new PageImpl<>(feedPost, pageable, postList.size());
    when(errorReportRepo.findFeedErrorReports(pageable)).thenReturn(pageImpl);

    // Act
    PagedResponse<FeedErrorReportResponseDTO> result = homeService.generateFeed(limit, offset, null);

    // Assert
    assertEquals(feedPost.size(), result.getData().size());

    for (int i = 0; i < feedPost.size(); i++) {
      assertEquals(
          feedPost.get(i).getNavigationId(), result.getData().get(i).getNavigationId());
      assertEquals(feedPost.get(i).getTitle(), result.getData().get(i).getTitle());
    }
  }

  @Test
  public void generateFeed_AuthenticatedUser() {
    // Arrange
    int pageNum = 0;
    int limit = 10;
    int offset = 0;
    Pageable pageable = PageRequest.of(pageNum, limit);
    UUID userId = user3Id; // Let's use user3

    List<ErrorReport> filteredPosts = postList.stream()
            .filter(post -> !Objects.equals(post.getAuthor().getId(), userId))
            .toList();

    List<FeedErrorReportResponseDTO> feedPost =
        filteredPosts
            .subList(0, Math.min(10, filteredPosts.size()))
            .stream()
            .map(
                post -> {
                  List<ReportTagResponseDTO> tags =
                      post.getTags() == null ? List.of() : post.getTags().stream()
                          .map(tag -> new ReportTagResponseDTO(tag.getId(), tag.getName(), tag.getDescription()))
                          .toList();

                  return FeedErrorReportResponseDTO.builder()
                      .navigationId(post.getId())
                      .title(post.getTitle())
                      .tags(tags)
                      .build();
                })
            .toList();

    feedPost.forEach(
        post ->
            when(tagRepo.findTagsByErrorReportId(post.getNavigationId()))
                .thenReturn(post.getTags()));

    Page<FeedErrorReportResponseDTO> pageImpl = new PageImpl<>(feedPost, pageable, filteredPosts.size());
    when(errorReportRepo.findFeedErrorReportsByAuthorIdNot(userId, pageable))
        .thenReturn(pageImpl);

    // Act
    PagedResponse<FeedErrorReportResponseDTO> result = homeService.generateFeed(limit, offset, userId);

    // Assert
    assertEquals(feedPost.size(), result.getData().size());

    for (int i = 0; i < feedPost.size(); i++) {
      assertEquals(
          feedPost.get(i).getNavigationId(), result.getData().get(i).getNavigationId());
      assertEquals(feedPost.get(i).getTitle(), result.getData().get(i).getTitle());
    }
  }
}
