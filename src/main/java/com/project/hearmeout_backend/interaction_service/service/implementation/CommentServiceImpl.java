package com.project.hearmeout_backend.interaction_service.service.implementation;

import com.project.hearmeout_backend.interaction_service.dto.request.CommentRequestDTO;
import com.project.hearmeout_backend.common_lib.exception.CommentNotFoundException;
import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.common_lib.exception.PostNotFoundException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.interaction_service.mapper.CommentMapper;
import com.project.hearmeout_backend.interaction_service.model.Comment;
import com.project.hearmeout_backend.post_service.model.Post;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.interaction_service.repository.CommentRepository;
import com.project.hearmeout_backend.post_service.repository.PostRepository;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl {

    private final UserServiceImpl userServiceImpl;
    private final CommentRepository commentRepo;
    private final PostRepository postRepo;
    private final UserRepository userRepo;

    @Transactional
    public void createNewComment(CommentRequestDTO commentRequestDTO, Long userId)
            throws UserNotFoundException, PostNotFoundException {
        User author = userServiceImpl.checkAndGetUserByUserId(userId);
        Post post = postRepo.findById(commentRequestDTO.getPostId())
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + commentRequestDTO.getPostId()));

        Comment newComment = CommentMapper.toCommentEntity(commentRequestDTO, post, author);
        commentRepo.save(newComment);

        author.setReputation(author.getReputation() + 2);
        userRepo.save(author);
    }

    @Transactional
    public void removeComment(Long commentId, Long userId)
            throws CommentNotFoundException {
        User author = userServiceImpl.checkAndGetUserByUserId(userId);
        Comment comment = checkAndGetComment(commentId);

        if(!comment.getAuthor().getId().equals(userId)) {
            throw new InvalidOperationException("Operation only allowed for account owner");
        }

        commentRepo.delete(comment);

        author.setReputation(author.getReputation() - 2);
        userRepo.save(author);
    }

    public Comment checkAndGetComment(Long commentId)
            throws CommentNotFoundException {
        return commentRepo.findById(commentId)
                .orElseThrow(() ->
                        new CommentNotFoundException("Comment not found with id: " + commentId)
                );
    }

    @Transactional
    public void updateCommentBody(Long commentId, String body, Long userId)
            throws CommentNotFoundException {
        Comment comment = checkAndGetComment(commentId);

        if(!Objects.equals(comment.getAuthor().getId(),userId)) {
            throw new InvalidOperationException("Operation only allowed for account owner");
        }

        comment.setBody(body);
        comment.markUpdatedAt();

        commentRepo.save(comment);
    }
}
