package com.project.hearmeout_backend.service.implementation;

import com.project.hearmeout_backend.dto.request.vote_request.VoteRequestDTO;
import com.project.hearmeout_backend.exception.InvalidOperationException;
import com.project.hearmeout_backend.model.Post;
import com.project.hearmeout_backend.model.User;
import com.project.hearmeout_backend.model.Vote;
import com.project.hearmeout_backend.model.enums.VoteType;
import com.project.hearmeout_backend.repository.PostRepository;
import com.project.hearmeout_backend.repository.UserRepository;
import com.project.hearmeout_backend.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VoteServiceImplTest {

    @Mock
    private VoteRepository voteRepo;
    @Mock
    private UserServiceImpl userService;
    @Mock
    private PostServiceImpl postService;
    @Mock
    private UserRepository userRepo;
    @Mock
    private PostRepository postRepo;

    @InjectMocks
    private VoteServiceImpl voteService;

    private User currUser;
    private User author;
    private Post post;
    private VoteRequestDTO voteRequestDTO;

    @BeforeEach
    void setUp() {
        currUser = User.builder()
                .username("currUser")
                .reputation(10)
                .build();
        currUser.setId(1L);

        author = User.builder()
                .username("author")
                .reputation(20)
                .build();
        author.setId(2L);

        post = Post.builder()
                .author(author)
                .score(5)
                .build();
        post.setId(100L);

        voteRequestDTO = new VoteRequestDTO();
        voteRequestDTO.setPostId(100L);
    }

    @Test
    void handleVote_SelfPost() {
        // Arrange
        author.setId(currUser.getId());
        voteRequestDTO.setVoteType(VoteType.UPVOTE);

        when(voteRepo.findByPostIdAndUserId(100L, 1L))
                .thenReturn(Optional.empty());
        when(userService.checkAndGetUserByUserId(1L))
                .thenReturn(currUser);
        when(postService.checkAndGetPost(100L))
                .thenReturn(post);

        // Act & Assert
        InvalidOperationException exception = assertThrows(InvalidOperationException.class, () -> {
            voteService.handleVote(voteRequestDTO, 1L);
        });

        assertEquals(
                "Invalid action: you cannot vote your own posts.",
                exception.getMessage()
        );

        verify(voteRepo, never())
                .save(any());
        verify(userRepo, never())
                .save(any());
        verify(postRepo, never())
                .save(any());
    }

    @Test
    void handleVote_NewUpvote() {
        // Arrange
        voteRequestDTO.setVoteType(VoteType.UPVOTE);

        when(voteRepo.findByPostIdAndUserId(100L, 1L))
                .thenReturn(Optional.empty());
        when(userService.checkAndGetUserByUserId(1L))
                .thenReturn(currUser);
        when(postService.checkAndGetPost(100L))
                .thenReturn(post);
        when(userService.checkAndGetUserByUserId(2L))
                .thenReturn(author);

        // Act
        voteService.handleVote(voteRequestDTO, 1L);

        // Assert
        assertEquals(
                21,
                author.getReputation()
        );
        assertEquals(
                6,
                post.getScore()
        );
        assertEquals(
                11,
                currUser.getReputation()
        );

        verify(userRepo)
                .save(author);
        verify(postRepo)
                .save(post);
        verify(userRepo)
                .save(currUser);

        // Since VoteMapper.toVoteEntity is used, we need to capture the saved vote to verify
        ArgumentCaptor<Vote> voteCaptor = ArgumentCaptor.forClass(Vote.class);
        verify(voteRepo).
                save(voteCaptor.capture());
        Vote savedVote = voteCaptor.getValue();

        assertEquals(
                VoteType.UPVOTE,
                savedVote.getVoteType()
        );
        assertEquals(
                currUser,
                savedVote.getUser()
        );
        assertEquals(
                post,
                savedVote.getPost()
        );
    }

    @Test
    void handleVote_NewDownvote() {
        // Arrange
        voteRequestDTO.setVoteType(VoteType.DOWNVOTE);

        when(voteRepo.findByPostIdAndUserId(100L, 1L))
                .thenReturn(Optional.empty());
        when(userService.checkAndGetUserByUserId(1L))
                .thenReturn(currUser);
        when(postService.checkAndGetPost(100L))
                .thenReturn(post);
        when(userService.checkAndGetUserByUserId(2L))
                .thenReturn(author);

        // Act
        voteService.handleVote(voteRequestDTO, 1L);

        // Assert
        assertEquals(
                19,
                author.getReputation()
        );
        assertEquals(
                4,
                post.getScore()
        );
        assertEquals(
                11,
                currUser.getReputation()
        );

        verify(userRepo)
                .save(author);
        verify(postRepo)
                .save(post);
        verify(userRepo)
                .save(currUser);
        verify(voteRepo)
                .save(any(Vote.class));
    }

    @Test
    void handleVote_ExistingVoteRemoved_Upvote() {
        // Arrange
        voteRequestDTO.setVoteType(VoteType.UPVOTE);
        Vote existingVote = Vote.builder()
                .user(currUser)
                .post(post)
                .voteType(VoteType.UPVOTE)
                .build();

        when(voteRepo.findByPostIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(existingVote));
        when(userService.checkAndGetUserByUserId(1L))
                .thenReturn(currUser);
        when(postService.checkAndGetPost(100L))
                .thenReturn(post);
        when(userService.checkAndGetUserByUserId(2L))
                .thenReturn(author);

        // Act
        voteService.handleVote(voteRequestDTO, 1L);

        // Assert
        assertEquals(
                19,
                author.getReputation()
        );
        assertEquals(
                4,
                post.getScore()
        );
        assertEquals(
                9,
                currUser.getReputation()
        );

        verify(userRepo)
                .save(author);
        verify(postRepo)
                .save(post);
        verify(userRepo)
                .save(currUser);
        verify(voteRepo)
                .removeVoteByPostIdAndUserId(100L, 1L);
    }

    @Test
    void handleVote_ExistingVoteRemoved_Downvote() {
        // Arrange
        voteRequestDTO.setVoteType(VoteType.DOWNVOTE);
        Vote existingVote = Vote.builder()
                .user(currUser)
                .post(post)
                .voteType(VoteType.DOWNVOTE)
                .build();

        when(voteRepo.findByPostIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(existingVote));
        when(userService.checkAndGetUserByUserId(1L))
                .thenReturn(currUser);
        when(postService.checkAndGetPost(100L))
                .thenReturn(post);
        when(userService.checkAndGetUserByUserId(2L))
                .thenReturn(author);

        // Act
        voteService.handleVote(voteRequestDTO, 1L);

        // Assert
        assertEquals(
                21,
                author.getReputation()
        );
        assertEquals(
                6,
                post.getScore()
        );
        assertEquals(
                9,
                currUser.getReputation()
        );

        verify(userRepo)
                .save(author);
        verify(postRepo)
                .save(post);
        verify(userRepo)
                .save(currUser);
        verify(voteRepo)
                .removeVoteByPostIdAndUserId(100L, 1L);
    }

    @Test
    void handleVote_ExistingVoteChanged_Upvote() {
        // Arrange
        voteRequestDTO.setVoteType(VoteType.UPVOTE);
        Vote existingVote = Vote.builder()
                .user(currUser)
                .post(post)
                .voteType(VoteType.DOWNVOTE)
                .build();

        when(voteRepo.findByPostIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(existingVote));
        when(userService.checkAndGetUserByUserId(1L))
                .thenReturn(currUser);
        when(postService.checkAndGetPost(100L))
                .thenReturn(post);
        when(userService.checkAndGetUserByUserId(2L))
                .thenReturn(author);

        // Act
        voteService.handleVote(voteRequestDTO, 1L);

        // Assert
        assertEquals(
                22,
                author.getReputation()
        );
        assertEquals(
                7,
                post.getScore()
        );
        assertEquals(
                10,
                currUser.getReputation()
        );

        assertEquals(
                VoteType.UPVOTE,
                existingVote.getVoteType()
        );

        verify(voteRepo)
                .save(existingVote);
        verify(postRepo)
                .save(post);
        verify(userRepo)
                .save(author);
        verify(userRepo, never())
                .save(currUser);
    }

    @Test
    public void handleVote_ExistingVoteChanged_Downvote() {
        //Arrange
        voteRequestDTO.setVoteType(VoteType.DOWNVOTE);
        Vote existingVote = Vote.builder()
                .user(currUser)
                .post(post)
                .voteType(VoteType.UPVOTE)
                .build();

        when(voteRepo.findByPostIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(existingVote));
        when(userService.checkAndGetUserByUserId(1L))
                .thenReturn(currUser);
        when(postService.checkAndGetPost(100L))
                .thenReturn(post);
        when(userService.checkAndGetUserByUserId(2L))
                .thenReturn(author);

        //Act
        voteService.handleVote(voteRequestDTO, 1L);

        //Assert
        assertEquals(
                18,
                author.getReputation()
        );
        assertEquals(
                3,
                post.getScore()
        );
        assertEquals(
                10,
                currUser.getReputation()
        );
        assertEquals(
                VoteType.DOWNVOTE,
                existingVote.getVoteType()
        );

        verify(voteRepo)
                .save(existingVote);
        verify(postRepo)
                .save(post);
        verify(userRepo)
                .save(author);
        verify(userRepo, never())
                .save(currUser);
    }
}
