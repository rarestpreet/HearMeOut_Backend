package com.project.hearmeout_backend.interaction_service.service.implementation;

import com.project.hearmeout_backend.common_lib.dto.PageData;
import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.common_lib.exception.CommentNotFoundException;
import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.common_lib.exception.PostNotFoundException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.interaction_service.dto.request.CommentRequestDTO;
import com.project.hearmeout_backend.interaction_service.dto.response.CommentResponseDTO;
import com.project.hearmeout_backend.interaction_service.mapper.CommentMapper;
import com.project.hearmeout_backend.interaction_service.model.Comment;
import com.project.hearmeout_backend.interaction_service.repository.CommentRepository;
import com.project.hearmeout_backend.post_service.model.Post;
import com.project.hearmeout_backend.post_service.repository.PostRepository;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl {

  private final UserServiceImpl userServiceImpl;
  private final CommentRepository commentRepo;
  private final PostRepository postRepo;
  private final UserRepository userRepo;

  public PagedResponse<CommentResponseDTO> getPostComments(
      Long postId, int limit, int offset, String username) throws PostNotFoundException {
    postRepo
        .findById(postId)
        .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));

    int page = offset / limit;
    Pageable pageable = PageRequest.of(Math.max(page, 0), limit);

    Page<CommentResponseDTO> commentsPage = commentRepo.findCommentsDTOByPostId(postId, pageable);

    List<CommentResponseDTO> comments = commentsPage.getContent();
    comments.forEach(
        c -> {
          CommentResponseDTO.CommentResponseDTOBuilder updatedComment =
              CommentResponseDTO.builder();
          updatedComment.operable(c.getAuthorUsername().equals(username));
          // Note: the original code tried to use a builder but didn't update the object.
          // I will fix the DTO logic here by using setters or directly setting if available.
          // Actually since it's a DTO, it has no setters. I'll recreate the DTO and replace it in
          // the list.
        });

    List<CommentResponseDTO> updatedComments =
        comments.stream()
            .map(
                c -> {
                  return new CommentResponseDTO(
                      c.getCommentId(),
                      c.getBody(),
                      c.getAuthorUsername(),
                      c.getNavigationPostId(),
                      c.getUpdatedAt(),
                      c.getAuthorUsername().equals(username));
                })
            .toList();

    return PagedResponse.<CommentResponseDTO>builder()
        .data(updatedComments)
        .pageData(
            PageData.builder()
                .hasMore(commentsPage.hasNext())
                .total(commentsPage.getTotalElements())
                .offset(offset)
                .limit(limit)
                .build())
        .build();
  }

  @Transactional
  public void createNewComment(CommentRequestDTO commentRequestDTO, Long userId)
      throws UserNotFoundException, PostNotFoundException {
    User author = userServiceImpl.checkAndGetUserByUserId(userId);
    Post post =
        postRepo
            .findById(commentRequestDTO.getPostId())
            .orElseThrow(
                () ->
                    new PostNotFoundException(
                        "Post not found with id: " + commentRequestDTO.getPostId()));

    Comment newComment = CommentMapper.toCommentEntity(commentRequestDTO, post, author);
    commentRepo.save(newComment);

    author.setReputation(author.getReputation() + 2);
    userRepo.save(author);
  }

  @Transactional
  public void removeComment(Long commentId, Long userId) throws CommentNotFoundException {
    User author = userServiceImpl.checkAndGetUserByUserId(userId);
    Comment comment = checkAndGetComment(commentId);

    if (!comment.getAuthor().getId().equals(userId)) {
      throw new InvalidOperationException("Operation only allowed for account owner");
    }

    commentRepo.delete(comment);
    author.setReputation(author.getReputation() - 2);
    userRepo.save(author);
  }

  public Comment checkAndGetComment(Long commentId) throws CommentNotFoundException {
    return commentRepo
        .findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));
  }

  @Transactional
  public void updateCommentBody(Long commentId, String body, Long userId)
      throws CommentNotFoundException {
    Comment comment = checkAndGetComment(commentId);

    if (!Objects.equals(comment.getAuthor().getId(), userId)) {
      throw new InvalidOperationException("Operation only allowed for account owner");
    }

    comment.setBody(body);
    comment.markUpdatedAt();
    commentRepo.save(comment);
  }
}
