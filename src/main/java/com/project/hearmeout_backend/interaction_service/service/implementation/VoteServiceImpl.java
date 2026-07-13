package com.project.hearmeout_backend.interaction_service.service.implementation;

import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.interaction_service.dto.request.VoteRequestDTO;
import com.project.hearmeout_backend.interaction_service.mapper.VoteMapper;
import com.project.hearmeout_backend.interaction_service.model.Vote;
import com.project.hearmeout_backend.interaction_service.model.enums.VoteType;
import com.project.hearmeout_backend.interaction_service.repository.VoteRepository;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.post_service.repository.ErrorReportRepository;
import com.project.hearmeout_backend.post_service.repository.SolutionRepository;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl {

  private final VoteRepository voteRepo;
  private final UserServiceImpl userServiceImpl;
  private final ErrorReportRepository errorReportRepo;
  private final SolutionRepository solutionRepo;
  private final UserRepository userRepo;

  @Transactional
  public void handleVote(VoteRequestDTO voteRequestDTO, UUID userId, VoteType newVoteType)
      throws BadRequestException {
    UUID parentId = voteRequestDTO.getParentId();
    PostType parentType = voteRequestDTO.getParentType();

    Vote existingVote =
        voteRepo.findByParentIdAndParentTypeAndUserId(parentId, parentType, userId).orElse(null);

    User currUser = userServiceImpl.checkAndGetUserByUserId(userId);

    // Resolve the author of the parent entity
    UUID authorId = resolveParentAuthorId(parentId, parentType);
    User author = userServiceImpl.checkAndGetUserByUserId(authorId);

    if (Objects.equals(author.getId(), currUser.getId())) {
      throw new InvalidOperationException("Invalid action: you cannot vote your own posts.");
    }

    if (!Objects.equals(newVoteType, VoteType.UPVOTE)
        && !Objects.equals(newVoteType, VoteType.DOWNVOTE)) {
      throw new BadRequestException("Invalid input provided, please try again.");
    }

    // New vote
    if (existingVote == null) {
      if (newVoteType == VoteType.UPVOTE) {
        author.setReputation(author.getReputation() + 1);
        updateParentScore(parentId, parentType, 1);
      } else {
        author.setReputation(author.getReputation() - 1);
        updateParentScore(parentId, parentType, -1);
      }
      userRepo.save(author);

      currUser.setReputation(currUser.getReputation() + 1);
      userRepo.save(currUser);

      Vote vote = VoteMapper.toVoteEntity(parentId, parentType, currUser, newVoteType);
      voteRepo.save(vote);

      return;
    }

    VoteType oldVoteType = existingVote.getVoteType();

    // Vote removed (same vote type toggled)
    if (oldVoteType == newVoteType) {
      if (newVoteType == VoteType.UPVOTE) {
        author.setReputation(author.getReputation() - 1);
        updateParentScore(parentId, parentType, -1);
      } else {
        author.setReputation(author.getReputation() + 1);
        updateParentScore(parentId, parentType, 1);
      }
      userRepo.save(author);

      currUser.setReputation(currUser.getReputation() - 1);
      userRepo.save(currUser);

      voteRepo.deleteByParentIdAndParentTypeAndUserId(parentId, parentType, userId);
    }
    // Vote changed (upvote→downvote or vice versa)
    else {
      if (newVoteType == VoteType.UPVOTE) {
        author.setReputation(author.getReputation() + 2);
        updateParentScore(parentId, parentType, 2);
      } else {
        author.setReputation(author.getReputation() - 2);
        updateParentScore(parentId, parentType, -2);
      }
      existingVote.setVoteType(newVoteType);
      voteRepo.save(existingVote);
      userRepo.save(author);
    }
  }

  private UUID resolveParentAuthorId(UUID parentId, PostType parentType) {
    if (parentType == PostType.ERROR_REPORT) {
      return errorReportRepo
          .findById(parentId)
          .orElseThrow(() -> new InvalidOperationException("Error report not found: " + parentId))
          .getAuthor()
          .getId();
    } else {
      return solutionRepo
          .findById(parentId)
          .orElseThrow(() -> new InvalidOperationException("Solution not found: " + parentId))
          .getAuthor()
          .getId();
    }
  }

  private void updateParentScore(UUID parentId, PostType parentType, int delta) {
    if (parentType == PostType.ERROR_REPORT) {
      errorReportRepo
          .findById(parentId)
          .ifPresent(
              report -> {
                report.setScore(report.getScore() + delta);
                errorReportRepo.save(report);
              });
    } else {
      solutionRepo
          .findById(parentId)
          .ifPresent(
              solution -> {
                solution.setScore(solution.getScore() + delta);
                solutionRepo.save(solution);
              });
    }
  }
}
