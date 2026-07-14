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
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.post_service.repository.ErrorReportRepository;
import com.project.hearmeout_backend.post_service.repository.SolutionRepository;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
  private final UserRepository userRepo;
  private final ErrorReportRepository errorReportRepo;
  private final SolutionRepository solutionRepo;

  public PagedResponse<CommentResponseDTO> getComments(
      UUID parentId, PostType parentType, int limit, int offset, String username) {
    int page = offset / limit;
    Pageable pageable = PageRequest.of(Math.max(page, 0), limit);

    Page<CommentResponseDTO> commentsPage =
        commentRepo.findCommentsByParent(parentId, parentType, pageable);

    List<CommentResponseDTO> updatedComments =
        commentsPage.getContent().stream()
            .map(
                c ->
                    new CommentResponseDTO(
                        c.getCommentId(),
                        c.getBody(),
                        c.getAuthorUsername(),
                        c.getParentId(),
                        c.getUpdatedAt(),
                        c.getAuthorUsername().equals(username)))
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
  public void createNewComment(CommentRequestDTO commentRequestDTO, UUID userId)
      throws UserNotFoundException {
    if (commentRequestDTO.getParentType() == PostType.ERROR_REPORT) {
      if (!errorReportRepo.existsById(commentRequestDTO.getParentId())) {
        throw new PostNotFoundException(
            "Error report not found: " + commentRequestDTO.getParentId());
      }
    } else if (commentRequestDTO.getParentType() == PostType.SOLUTION) {
      if (!solutionRepo.existsById(commentRequestDTO.getParentId())) {
        throw new PostNotFoundException("Solution not found: " + commentRequestDTO.getParentId());
      }
    }

    User author = userServiceImpl.checkAndGetUserByUserId(userId);

    Comment newComment = CommentMapper.toCommentEntity(commentRequestDTO, author);
    commentRepo.save(newComment);

    author.setReputation(author.getReputation() + 2);
    userRepo.save(author);
  }

  @Transactional
  public void removeComment(UUID commentId, UUID userId) throws CommentNotFoundException {
    User author = userServiceImpl.checkAndGetUserByUserId(userId);
    Comment comment = checkAndGetComment(commentId);

    if (!comment.getAuthor().getId().equals(userId)) {
      throw new InvalidOperationException("Operation only allowed for account owner");
    }

    commentRepo.delete(comment);
    author.setReputation(author.getReputation() - 2);
    userRepo.save(author);
  }

  public Comment checkAndGetComment(UUID commentId) throws CommentNotFoundException {
    return commentRepo
        .findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));
  }

  @Transactional
  public void updateCommentBody(UUID commentId, String body, UUID userId)
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
