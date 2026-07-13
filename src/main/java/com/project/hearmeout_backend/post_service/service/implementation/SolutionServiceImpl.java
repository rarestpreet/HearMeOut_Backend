package com.project.hearmeout_backend.post_service.service.implementation;

import com.project.hearmeout_backend.common_lib.dto.PageData;
import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.common_lib.exception.PostNotFoundException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.interaction_service.dto.response.CommentResponseDTO;
import com.project.hearmeout_backend.interaction_service.model.Vote;
import com.project.hearmeout_backend.interaction_service.model.enums.VoteType;
import com.project.hearmeout_backend.interaction_service.repository.CommentRepository;
import com.project.hearmeout_backend.interaction_service.repository.VoteRepository;
import com.project.hearmeout_backend.post_service.dto.request.AcceptSolutionRequestDTO;
import com.project.hearmeout_backend.post_service.dto.request.SolutionSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.dto.response.SolutionResponseDTO;
import com.project.hearmeout_backend.post_service.mapper.SolutionMapper;
import com.project.hearmeout_backend.post_service.model.ErrorReport;
import com.project.hearmeout_backend.post_service.model.Solution;
import com.project.hearmeout_backend.post_service.model.enums.ErrorReportStatus;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.post_service.model.enums.SolutionStatus;
import com.project.hearmeout_backend.post_service.repository.ErrorReportRepository;
import com.project.hearmeout_backend.post_service.repository.SolutionRepository;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolutionServiceImpl {

  private final SolutionRepository solutionRepo;
  private final ErrorReportRepository errorReportRepo;
  private final UserServiceImpl userServiceImpl;
  private final VoteRepository voteRepo;
  private final CommentRepository commentRepo;
  private final UserRepository userRepo;

  public Solution checkAndGetSolution(UUID solutionId) throws PostNotFoundException {
    return solutionRepo
        .findById(solutionId)
        .orElseThrow(
            () -> new PostNotFoundException("Solution not found with id: " + solutionId));
  }

  @Transactional
  public void submitSolution(
      UUID errorReportId, SolutionSubmitRequestDTO dto, UUID userId)
      throws UserNotFoundException, PostNotFoundException {
    ErrorReport errorReport =
        errorReportRepo
            .findById(errorReportId)
            .orElseThrow(
                () ->
                    new PostNotFoundException(
                        "Error report not found with id: " + errorReportId));

    if (Objects.equals(userId, errorReport.getAuthor().getId())) {
      throw new InvalidOperationException("You cannot submit a solution to your own error report.");
    }

    if (Objects.equals(errorReport.getStatus(), ErrorReportStatus.CLOSED)
        || Objects.equals(errorReport.getStatus(), ErrorReportStatus.RESOLVED)) {
      throw new InvalidOperationException("Error report is already resolved/closed.");
    }

    User author = userServiceImpl.checkAndGetUserByUserId(userId);
    author.setReputation(author.getReputation() + 6);
    userRepo.save(author);

    errorReport.setStatus(ErrorReportStatus.IN_PROGRESS);

    Solution newSolution = SolutionMapper.toEntity(dto, errorReport, author);
    newSolution.setAuthor(author);
    solutionRepo.save(newSolution);
  }

  @Transactional(readOnly = true)
  public PagedResponse<SolutionResponseDTO> getSolutions(
      UUID errorReportId, UUID currUserId, String currUsername, int limit, int offset)
      throws PostNotFoundException {
    errorReportRepo
        .findById(errorReportId)
        .orElseThrow(
            () ->
                new PostNotFoundException(
                    "Error report not found with id: " + errorReportId));

    int page = offset / limit;
    Pageable solutionPageable = PageRequest.of(Math.max(page, 0), limit);

    Page<SolutionResponseDTO> solutionsPage =
        solutionRepo.findSolutionsByErrorReportId(errorReportId, solutionPageable);

    List<SolutionResponseDTO> solutions = solutionsPage.getContent();
    List<UUID> solutionIds = solutions.stream().map(SolutionResponseDTO::getId).toList();

    List<CommentResponseDTO> allSolutionComments =
        solutionIds.isEmpty()
            ? List.of()
            : solutionIds.stream()
                .map(
                    id ->
                        commentRepo
                            .findCommentsByParent(id, PostType.SOLUTION, PageRequest.of(0, 5))
                            .getContent())
                .flatMap(List::stream)
                .toList();

    List<SolutionResponseDTO> enrichedSolutions =
        solutions.stream()
            .map(
                solution -> {
                  Vote solutionVote =
                      voteRepo
                          .findByParentIdAndParentTypeAndUserId(
                              solution.getId(), PostType.SOLUTION, currUserId)
                          .orElse(null);

                  List<CommentResponseDTO> solutionComments =
                      allSolutionComments.stream()
                          .filter(c -> c.getParentId().equals(solution.getId()))
                          .map(
                              c ->
                                  new CommentResponseDTO(
                                      c.getCommentId(),
                                      c.getBody(),
                                      c.getAuthorUsername(),
                                      c.getParentId(),
                                      c.getUpdatedAt(),
                                      c.getAuthorUsername().equals(currUsername)))
                          .toList();

                  return SolutionResponseDTO.builder()
                      .id(solution.getId())
                      .authorUsername(solution.getAuthorUsername())
                      .explanation(solution.getExplanation())
                      .voted(solutionVote != null)
                      .voteType(solutionVote != null ? solutionVote.getVoteType() : null)
                      .updatedAt(solution.getUpdatedAt())
                      .status(solution.getStatus())
                      .comments(solutionComments)
                      .hasMoreComments(false)
                      .score(solution.getScore())
                      .operable(solution.getAuthorUsername().equals(currUsername))
                      .build();
                })
            .toList();

    return PagedResponse.<SolutionResponseDTO>builder()
        .data(enrichedSolutions)
        .pageData(
            PageData.builder()
                .hasMore(solutionsPage.hasNext())
                .total(solutionsPage.getTotalElements())
                .offset(offset)
                .limit(limit)
                .build())
        .build();
  }

  @Transactional
  public void handleSolutionStatus(AcceptSolutionRequestDTO dto, UUID currUserId) {
    ErrorReport errorReport =
        errorReportRepo
            .findById(dto.getErrorReportId())
            .orElseThrow(
                () ->
                    new PostNotFoundException(
                        "Error report not found with id: " + dto.getErrorReportId()));

    Solution solution = checkAndGetSolution(dto.getSolutionId());
    User solutionAuthor = userServiceImpl.checkAndGetUserByUserId(solution.getAuthor().getId());
    User reportAuthor = userServiceImpl.checkAndGetUserByUserId(errorReport.getAuthor().getId());

    if (!Objects.equals(reportAuthor.getId(), currUserId)) {
      throw new InvalidOperationException(
          "You can only accept solutions for your own error reports.");
    }

    if (!Objects.equals(solution.getErrorReport().getId(), errorReport.getId())) {
      throw new InvalidOperationException(
          "This solution does not belong to the specified error report.");
    }

    SolutionStatus currSolutionStatus = solution.getStatus();
    ErrorReportStatus currReportStatus = errorReport.getStatus();

    // Report is not yet resolved, author wants to accept this solution
    if (Objects.equals(currReportStatus, ErrorReportStatus.IN_PROGRESS)
        || Objects.equals(currReportStatus, ErrorReportStatus.OPEN)) {
      reportAuthor.setReputation(reportAuthor.getReputation() + 3);
      solutionAuthor.setReputation(solutionAuthor.getReputation() + 7);
      solution.setScore(solution.getScore() + 5);
      errorReport.setStatus(ErrorReportStatus.RESOLVED);
      solution.setStatus(SolutionStatus.ACCEPTED);

      errorReportRepo.save(errorReport);
      solutionRepo.save(solution);
      userRepo.save(solutionAuthor);
      userRepo.save(reportAuthor);

      return;
    }

    // Report is already resolved, author wants to un-accept
    if (Objects.equals(currSolutionStatus, SolutionStatus.ACCEPTED)) {
      reportAuthor.setReputation(reportAuthor.getReputation() - 3);
      solutionAuthor.setReputation(solutionAuthor.getReputation() - 7);
      solution.setScore(solution.getScore() - 5);
      errorReport.setStatus(ErrorReportStatus.IN_PROGRESS);
      solution.setStatus(SolutionStatus.PENDING);
    }
    // Report is already resolved, author wants to accept a different solution
    else {
      List<Solution> acceptedSolutions =
          errorReport.getSolutions().stream()
              .filter(s -> Objects.equals(s.getStatus(), SolutionStatus.ACCEPTED))
              .toList();

      if (acceptedSolutions.size() != 1) {
        log.warn("Invalid accepted solutions for resolved error report {}", errorReport.getId());
      }
      Solution olderAccepted = acceptedSolutions.getFirst();
      User olderAcceptedAuthor =
          userServiceImpl.checkAndGetUserByUserId(olderAccepted.getAuthor().getId());

      olderAccepted.setStatus(SolutionStatus.PENDING);
      olderAccepted.setScore(olderAccepted.getScore() - 5);
      olderAcceptedAuthor.setReputation(olderAcceptedAuthor.getReputation() - 7);

      solutionRepo.save(olderAccepted);
      userRepo.save(olderAcceptedAuthor);

      solutionAuthor.setReputation(solutionAuthor.getReputation() + 7);
      solution.setScore(solution.getScore() + 5);
      solution.setStatus(SolutionStatus.ACCEPTED);
    }

    errorReportRepo.save(errorReport);
    solutionRepo.save(solution);
    userRepo.save(solutionAuthor);
    userRepo.save(reportAuthor);
  }

  @Transactional
  public void updateSolution(UUID solutionId, SolutionSubmitRequestDTO dto, UUID currUserId) {
    Solution solution = checkAndGetSolution(solutionId);

    if (!Objects.equals(solution.getAuthor().getId(), currUserId)) {
      throw new InvalidOperationException("You cannot modify this solution.");
    }

    if (Objects.equals(solution.getStatus(), SolutionStatus.ACCEPTED)) {
      throw new InvalidOperationException("Cannot modify this solution, it is finalized.");
    }

    solution.setProbableCause(dto.getProbableCause());
    solution.setExplanation(dto.getExplanation());
    solution.setCodeChange(dto.getCodeChange());
    solution.setLanguage(dto.getLanguage());
    solution.setLanguageVersion(dto.getLanguageVersion());
    solution.setFramework(dto.getFramework());
    solution.setFrameworkVersion(dto.getFrameworkVersion());
    solution.setOs(dto.getOs());
    solution.setOsVersion(dto.getOsVersion());
    solution.markUpdatedAt();

    solutionRepo.save(solution);
  }

  @Transactional
  public void deleteSolution(UUID solutionId, UUID currUserId) {
    Solution solution = checkAndGetSolution(solutionId);

    if (!Objects.equals(solution.getAuthor().getId(), currUserId)) {
      throw new InvalidOperationException("You cannot delete this solution.");
    }

    if (Objects.equals(solution.getStatus(), SolutionStatus.ACCEPTED)) {
      throw new InvalidOperationException("Cannot delete this solution, it is finalized.");
    }

    User solutionAuthor = userServiceImpl.checkAndGetUserByUserId(solution.getAuthor().getId());
    List<Vote> votes =
        voteRepo.findAllByParentIdAndParentType(solutionId, PostType.SOLUTION);

    votes.forEach(
        vote -> {
          vote.getUser().setReputation(vote.getUser().getReputation() - 1);

          if (Objects.equals(vote.getVoteType(), VoteType.UPVOTE)) {
            solutionAuthor.setReputation(solutionAuthor.getReputation() - 1);
          } else {
            solutionAuthor.setReputation(solutionAuthor.getReputation() + 1);
          }

          voteRepo.save(vote);
        });

    solutionAuthor.setReputation(solutionAuthor.getReputation() - 6);

    solutionRepo.delete(solution);
  }
}
