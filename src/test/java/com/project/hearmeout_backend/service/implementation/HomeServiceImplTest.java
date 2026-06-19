package com.project.hearmeout_backend.service.implementation;

import com.project.hearmeout_backend.feed_service.service.implementation.HomeServiceImpl;
import com.project.hearmeout_backend.feed_service.dto.response.FeedQuestionResponseDTO;
import com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO;
import com.project.hearmeout_backend.feed_service.dto.response.HomeUserProfileResponseDTO;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.post_service.model.Post;
import com.project.hearmeout_backend.post_service.model.Tag;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.post_service.repository.PostRepository;
import com.project.hearmeout_backend.post_service.repository.TagRepository;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HomeServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PostRepository postRepo;

    @Mock
    private TagRepository tagRepo;

    @InjectMocks
    private HomeServiceImpl homeService;

    private List<Tag> tagList;
    private List<Post> postList;
    private List<User> userList;

    @BeforeEach
    public void setUp() {
        // USERS
        User user1 = User.builder()
                .username("test1")
                .email("test1@gmail.com")
                .build();
        user1.setId(1L);

        User user2 = User.builder()
                .username("test2")
                .email("test2@gmail.com")
                .build();
        user2.setId(2L);

        User user3 = User.builder()
                .username("test3")
                .email("test3@gmail.com")
                .build();
        user3.setId(3L);

        userList = List.of(user1, user2, user3);

        //TAGS
        Tag tag1 = Tag.builder()
                .name("Test1")
                .build();
        tag1.setId(1L);

        Tag tag2 = Tag.builder()
                .name("Test2")
                .build();
        tag2.setId(2L);

        Tag tag3 = Tag.builder()
                .name("Test3")
                .build();
        tag3.setId(3L);

        Tag tag4 = Tag.builder()
                .name("Test4")
                .build();
        tag4.setId(4L);

        tagList = List.of(tag1, tag2, tag3, tag4);

        // POSTS
        Post post1 = Post.builder()
                .title("Answer 1")
                .postType(PostType.ANSWER)
                .author(user2)
                .build();
        post1.setId(1L);

        Post post2 = Post.builder()
                .title("Question 1")
                .postType(PostType.QUESTION)
                .tags(List.of(tag1, tag2))
                .author(user3)
                .build();
        post2.setId(2L);

        Post post3 = Post.builder()
                .title("Question 2")
                .postType(PostType.QUESTION)
                .author(user1)
                .tags(List.of(tag2, tag3))
                .build();
        post3.setId(3L);

        Post post4 = Post.builder()
                .title("Question 3")
                .postType(PostType.QUESTION)
                .author(user2)
                .tags(List.of(tag1, tag3, tag4))
                .build();
        post4.setId(4L);

        Post post5 = Post.builder()
                .title("Answer 2")
                .postType(PostType.ANSWER)
                .author(user3)
                .build();
        post5.setId(5L);

        Post post6 = Post.builder()
                .title("Question 4")
                .postType(PostType.QUESTION)
                .author(user1)
                .tags(List.of(tag1, tag4))
                .build();
        post6.setId(6L);

        Post post7 = Post.builder()
                .title("Answer 3")
                .postType(PostType.ANSWER)
                .author(user2)
                .build();
        post7.setId(7L);

        Post post8 = Post.builder()
                .title("Question 5")
                .postType(PostType.QUESTION)
                .author(user3)
                .tags(List.of(tag2, tag4))
                .build();
        post8.setId(8L);

        Post post9 = Post.builder()
                .title("Question 6")
                .postType(PostType.QUESTION)
                .author(user1)
                .tags(List.of(tag3, tag4))
                .build();
        post9.setId(9L);

        postList = List.of(post1, post2, post3, post4, post5, post6, post7, post8, post9);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null"
    }, nullValues = "null")
    public void getUserProfile_GuestUser(Long userId) {
        //Act
        HomeUserProfileResponseDTO result = homeService.getUserProfile(userId);

        //Assert
        assertNull(
                result.getUsername()
        );
        assertNull(
                result.getUserNavigationId()
        );
        assertEquals(
                0,
                result.getRoles().size()
        );
    }

    @ParameterizedTest
    @ValueSource(
            longs = {4}
    )
    public void getUserProfile_InvalidUser(Long userId) {
        //Arrange
        HomeUserProfileResponseDTO currUser =
                userList.stream()
                        .filter(user ->
                                Objects.equals(user.getId(), userId)
                        )
                        .findFirst()
                        .map(user ->
                                HomeUserProfileResponseDTO.builder()
                                        .userNavigationId(userId)
                                        .username(user.getUsername())
                                        .build()
                        )
                        .orElse(null);

        when(userRepo.getHomeUserProfileById(userId))
                .thenReturn(Optional.ofNullable(currUser));

        //Act and Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class, () ->
                        homeService.getUserProfile(userId)
        );

        assertEquals(
                "User with id:  " + userId + " was not found",
                exception.getMessage()
        );
    }

    @ParameterizedTest
    @ValueSource(
            longs = {1, 3}
    )
    public void getUserProfile_AuthenticatedUser(Long userId) {
        //Arrange
        HomeUserProfileResponseDTO currUser =
                userList.stream()
                        .filter(user ->
                                Objects.equals(user.getId(), userId)
                        )
                        .findFirst()
                        .map(user ->
                                HomeUserProfileResponseDTO.builder()
                                        .userNavigationId(userId)
                                        .username(user.getUsername())
                                        .build()
                        )
                        .orElse(null);

        when(userRepo.getHomeUserProfileById(userId))
                .thenReturn(Optional.ofNullable(currUser));

        //Act
        HomeUserProfileResponseDTO result =
                homeService.getUserProfile(userId);

        //Assert
        assertEquals(
                currUser,
                result
        );
    }

    @ParameterizedTest
    @CsvSource(value = {
            "-1, null",
            "0, null",
            "1, null"
    }, nullValues = "null")
    public void generateFeed_GuestUser(int pageNum, Long userId) {
        //Arrange
        pageNum = Math.max(0, pageNum);
        Pageable pageable = PageRequest.of(pageNum, 10);

        int start = pageNum * 10;

        List<Post> questions = postList.stream()
                .filter(post ->
                        Objects.equals(post.getPostType(), PostType.QUESTION)
                )
                .toList();

        List<FeedQuestionResponseDTO> feedPost =
                questions.subList(
                                Math.min(start, questions.size()),
                                Math.min(start + 10, questions.size())
                        ).stream()
                        .map(post -> {
                                    List<TagResponseDTO> tags =
                                            post.getTags().stream()
                                                    .map(tag ->
                                                            TagResponseDTO.builder()
                                                                    .tagId(tag.getId())
                                                                    .build()
                                                    )
                                                    .toList();

                                    return FeedQuestionResponseDTO.builder()
                                            .navigationPostId(post.getId())
                                            .title(post.getTitle())
                                            .tags(tags)
                                            .build();
                                }
                        ).toList();

        feedPost.forEach(post ->
                when(tagRepo.findTagsDTOByPostId(post.getNavigationPostId()))
                        .thenReturn(post.getTags())
        );

        when(postRepo.findFeedPostsDTOByPostType(PostType.QUESTION, pageable))
                .thenReturn(feedPost);

        //Act
        List<FeedQuestionResponseDTO> result =
                homeService.generateFeed(pageNum, userId);

        //Assert
        assertEquals(
                feedPost,
                result
        );

        if (start < feedPost.size()) {
            assertEquals(
                    feedPost.getFirst().getNavigationPostId(),
                    result.getFirst().getNavigationPostId()
            );
        }
    }

    @ParameterizedTest
    @CsvSource(value = {
            "-1, 3",
            "0, 3",
            "1, 2"
    })
    public void generateFeed_AuthenticatedUser(int pageNum, Long userId) {
        //Arrange
        pageNum = Math.max(0, pageNum);
        Pageable pageable = PageRequest.of(pageNum, 10);

        int start = pageNum * 10;

        User currUser = userList.stream()
                .filter(user ->
                        Objects.equals(user.getId(), userId))
                .findFirst()
                .get();

        List<Post> questions = postList.stream()
                .filter(post ->
                        Objects.equals(post.getPostType(), PostType.QUESTION) &&
                                !Objects.equals(post.getAuthor().getId(), userId)
                )
                .toList();

        List<FeedQuestionResponseDTO> feedPost =
                questions.subList(
                                Math.min(start, questions.size()),
                                Math.min(start + 10, questions.size())
                        ).stream()
                        .map(post -> {
                                    List<TagResponseDTO> tags =
                                            post.getTags().stream()
                                                    .map(tag ->
                                                            TagResponseDTO.builder()
                                                                    .tagId(tag.getId())
                                                                    .build()
                                                    )
                                                    .toList();

                                    return FeedQuestionResponseDTO.builder()
                                            .navigationPostId(post.getId())
                                            .title(post.getTitle())
                                            .tags(tags)
                                            .build();
                                }
                        ).toList();

        feedPost.forEach(post ->
                when(tagRepo.findTagsDTOByPostId(post.getNavigationPostId()))
                        .thenReturn(post.getTags())
        );

        when(postRepo.findFeedPostsDTOByPostTypeAndAuthorIdNot(PostType.QUESTION, userId, pageable))
                .thenReturn(feedPost);

        //Act
        List<FeedQuestionResponseDTO> result =
                homeService.generateFeed(pageNum, userId);

        //Assert
        assertEquals(
                feedPost,
                result
        );

        result.forEach(post ->
                assertNotEquals(post.getAuthorUsername(), currUser.getUsername())
        );

        if (start < feedPost.size()) {
            assertEquals(
                    feedPost.getFirst().getNavigationPostId(),
                    result.getFirst().getNavigationPostId()
            );
        }
    }
}