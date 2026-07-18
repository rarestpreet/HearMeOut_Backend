package com.project.hearmeout_backend.post_service.service.implementation;

import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.common_lib.exception.PostNotFoundException;
import com.project.hearmeout_backend.common_lib.exception.TagNotFoundException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.interaction_service.dto.response.CommentResponseDTO;
import com.project.hearmeout_backend.interaction_service.model.Vote;
import com.project.hearmeout_backend.interaction_service.model.enums.VoteType;
import com.project.hearmeout_backend.interaction_service.repository.CommentRepository;
import com.project.hearmeout_backend.interaction_service.repository.VoteRepository;
import com.project.hearmeout_backend.post_service.dto.request.ErrorReportSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.dto.response.ErrorReportResponseDTO;
import com.project.hearmeout_backend.post_service.dto.response.SolutionResponseDTO;
import com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO;
import com.project.hearmeout_backend.post_service.mapper.ErrorReportMapper;
import com.project.hearmeout_backend.post_service.model.ErrorReport;
import com.project.hearmeout_backend.post_service.model.Framework;
import com.project.hearmeout_backend.post_service.model.OperatingSystem;
import com.project.hearmeout_backend.post_service.model.ProgrammingLanguage;
import com.project.hearmeout_backend.post_service.model.Tag;
import com.project.hearmeout_backend.post_service.model.enums.ErrorReportStatus;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.post_service.repository.ErrorReportRepository;
import com.project.hearmeout_backend.post_service.repository.FrameworkRepository;
import com.project.hearmeout_backend.post_service.repository.OperatingSystemRepository;
import com.project.hearmeout_backend.post_service.repository.ProgrammingLanguageRepository;
import com.project.hearmeout_backend.post_service.repository.SolutionRepository;
import com.project.hearmeout_backend.post_service.repository.TagRepository;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import java.util.ArrayList;
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
public class ErrorReportServiceImpl {

  private final ErrorReportRepository errorReportRepo;
  private final SolutionRepository solutionRepo;
  private final TagRepository tagRepo;
  private final UserServiceImpl userServiceImpl;
  private final VoteRepository voteRepo;
  private final CommentRepository commentRepo;
  private final UserRepository userRepo;
  private final ProgrammingLanguageRepository languageRepo;
  private final FrameworkRepository frameworkRepo;
  private final OperatingSystemRepository osRepo;

  public ErrorReport checkAndGetErrorReport(UUID errorReportId) throws PostNotFoundException {
    return errorReportRepo
        .findById(errorReportId)
        .orElseThrow(
            () -> new PostNotFoundException("Error report not found with id: " + errorReportId));
  }

  @Transactional
  public void submitErrorReport(ErrorReportSubmitRequestDTO dto, UUID userId)
      throws UserNotFoundException, TagNotFoundException {
    User author = userServiceImpl.checkAndGetUserByUserId(userId);
    List<Tag> tags = tagRepo.findAllById(dto.getTagIds());

    if (dto.getTagIds().size() != tags.size()) {
      throw new TagNotFoundException("Some tags do not exist");
    }

    author.setReputation(author.getReputation() + 4);
    userRepo.save(author);

    ProgrammingLanguage language =
        languageRepo
            .findByNameIgnoreCase(dto.getLanguage())
            .orElseGet(
                () ->
                    languageRepo.save(
                        ProgrammingLanguage.builder()
                            .name(dto.getLanguage().toUpperCase())
                            .build()));

    Framework framework = null;
    if (dto.getFramework() != null && !dto.getFramework().isBlank()) {
      framework =
          frameworkRepo
              .findByNameIgnoreCase(dto.getFramework())
              .orElseGet(
                  () ->
                      frameworkRepo.save(
                          Framework.builder().name(dto.getFramework().toUpperCase()).build()));
    }

    OperatingSystem os = null;
    if (dto.getOs() != null && !dto.getOs().isBlank()) {
      os =
          osRepo
              .findByNameIgnoreCase(dto.getOs())
              .orElseGet(
                  () ->
                      osRepo.save(
                          OperatingSystem.builder().name(dto.getOs().toUpperCase()).build()));
    }

    ErrorReport newReport = ErrorReportMapper.toEntity(dto, author, tags, language, framework, os);
    newReport.setAuthor(author);
    errorReportRepo.save(newReport);

    tagRepo.incrementUsageCount(dto.getTagIds());
  }

  @Transactional(readOnly = true)
  public ErrorReportResponseDTO getErrorReportDetails(
      UUID errorReportId, UUID currUserId, String currUsername, int limit, int offset)
      throws PostNotFoundException {
    ErrorReportResponseDTO reportResponse =
        errorReportRepo
            .findErrorReportDetailsDTO(errorReportId)
            .orElseThrow(
                () ->
                    new PostNotFoundException("Error report not found with id: " + errorReportId));

    Vote currUserVote =
        voteRepo
            .findByParentIdAndParentTypeAndUserId(errorReportId, PostType.ERROR_REPORT, currUserId)
            .orElse(null);

    int page = offset / limit;
    Pageable solutionPageable = PageRequest.of(Math.max(page, 0), limit);
    Pageable commentPageable = PageRequest.of(0, 5);

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
                                  CommentResponseDTO.builder()
                                      .commentId(c.getCommentId())
                                      .body(c.getBody())
                                      .authorUsername(c.getAuthorUsername())
                                      .parentId(c.getParentId())
                                      .updatedAt(c.getUpdatedAt())
                                      .operable(c.getAuthorUsername().equals(currUsername))
                                      .type(c.getType())
                                      .build())
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
                      .language(solution.getLanguage())
                      .languageVersion(solution.getLanguageVersion())
                      .framework(solution.getFramework())
                      .frameworkVersion(solution.getFrameworkVersion())
                      .os(solution.getOs())
                      .osVersion(solution.getOsVersion())
                      .probableCause(solution.getProbableCause())
                      .codeChange(solution.getCodeChange())
                      .build();
                })
            .toList();

    List<TagResponseDTO> tags =
        tagRepo.findTagsByErrorReportId(errorReportId).stream()
            .map(
                t ->
                    TagResponseDTO.builder()
                        .tagId(t.getTagId())
                        .name(t.getName())
                        .description(t.getDescription())
                        .build())
            .toList();

    Page<CommentResponseDTO> commentsPage =
        commentRepo.findCommentsByParent(errorReportId, PostType.ERROR_REPORT, commentPageable);
    List<CommentResponseDTO> comments =
        commentsPage.getContent().stream()
            .map(
                c ->
                    CommentResponseDTO.builder()
                        .commentId(c.getCommentId())
                        .body(c.getBody())
                        .authorUsername(c.getAuthorUsername())
                        .parentId(c.getParentId())
                        .updatedAt(c.getUpdatedAt())
                        .operable(c.getAuthorUsername().equals(currUsername))
                        .type(c.getType())
                        .build())
            .toList();

    return ErrorReportResponseDTO.builder()
        .id(errorReportId)
        .title(reportResponse.getTitle())
        .description(reportResponse.getDescription())
        .solutions(enrichedSolutions)
        .authorUsername(reportResponse.getAuthorUsername())
        .tags(tags)
        .voted(currUserVote != null)
        .voteType(currUserVote != null ? currUserVote.getVoteType() : null)
        .comments(comments)
        .hasMoreSolutions(solutionsPage.hasNext())
        .hasMoreComments(commentsPage.hasNext())
        .status(reportResponse.getStatus())
        .score(reportResponse.getScore())
        .operable(reportResponse.getAuthorUsername().equals(currUsername))
        .language(reportResponse.getLanguage())
        .languageVersion(reportResponse.getLanguageVersion())
        .framework(reportResponse.getFramework())
        .frameworkVersion(reportResponse.getFrameworkVersion())
        .os(reportResponse.getOs())
        .osVersion(reportResponse.getOsVersion())
        .errorType(reportResponse.getErrorType())
        .reproductionSteps(reportResponse.getReproductionSteps())
        .repositoryUrl(reportResponse.getRepositoryUrl())
        .branch(reportResponse.getBranch())
        .commitHash(reportResponse.getCommitHash())
        .filePath(reportResponse.getFilePath())
        .relevantCode(reportResponse.getRelevantCode())
        .relevantLog(reportResponse.getRelevantLog())
        .updatedAt(reportResponse.getUpdatedAt())
        .build();
  }

  @Transactional
  public void updateErrorReport(
      UUID errorReportId, ErrorReportSubmitRequestDTO dto, UUID currUserId) {
    ErrorReport report = checkAndGetErrorReport(errorReportId);

    if (!Objects.equals(report.getAuthor().getId(), currUserId)) {
      throw new InvalidOperationException("You cannot modify this error report.");
    }

    if (Objects.equals(report.getStatus(), ErrorReportStatus.RESOLVED)
        || Objects.equals(report.getStatus(), ErrorReportStatus.CLOSED)) {
      throw new InvalidOperationException("Cannot modify this error report, already resolved.");
    }

    List<UUID> oldTagIds = report.getTags().stream().map(Tag::getId).toList();
    List<UUID> newTagIds = dto.getTagIds();

    List<Tag> tags = tagRepo.findAllById(newTagIds);
    if (tags.size() != newTagIds.size()) {
      throw new TagNotFoundException("Some tags do not exist");
    }

    List<UUID> removedTagIds =
        new ArrayList<>(oldTagIds.stream().filter(id -> !newTagIds.contains(id)).toList());
    List<UUID> addedTagIds =
        new ArrayList<>(newTagIds.stream().filter(id -> !oldTagIds.contains(id)).toList());

    if (!removedTagIds.isEmpty()) {
      tagRepo.decrementUsageCount(removedTagIds);
    }
    if (!addedTagIds.isEmpty()) {
      tagRepo.incrementUsageCount(addedTagIds);
    }

    report.setTitle(dto.getTitle());
    report.setDescription(dto.getDescription());
    report.setReproductionSteps(dto.getReproductionSteps());
    report.setErrorType(dto.getErrorType());
    report.setRepositoryUrl(dto.getRepositoryUrl());
    report.setBranch(dto.getBranch());
    report.setCommitHash(dto.getCommitHash());
    report.setFilePath(dto.getFilePath());
    report.setRelevantCode(dto.getRelevantCode());
    report.setRelevantLog(dto.getRelevantLog());
    ProgrammingLanguage language =
        languageRepo
            .findByNameIgnoreCase(dto.getLanguage())
            .orElseGet(
                () ->
                    languageRepo.save(
                        ProgrammingLanguage.builder()
                            .name(dto.getLanguage().toUpperCase())
                            .build()));

    Framework framework = null;
    if (dto.getFramework() != null && !dto.getFramework().isBlank()) {
      framework =
          frameworkRepo
              .findByNameIgnoreCase(dto.getFramework())
              .orElseGet(
                  () ->
                      frameworkRepo.save(
                          Framework.builder().name(dto.getFramework().toUpperCase()).build()));
    }

    OperatingSystem os = null;
    if (dto.getOs() != null && !dto.getOs().isBlank()) {
      os =
          osRepo
              .findByNameIgnoreCase(dto.getOs())
              .orElseGet(
                  () ->
                      osRepo.save(
                          OperatingSystem.builder().name(dto.getOs().toUpperCase()).build()));
    }

    report.setLanguage(language);
    report.setLanguageVersion(dto.getLanguageVersion());
    report.setFramework(framework);
    report.setFrameworkVersion(dto.getFrameworkVersion());
    report.setOs(os);
    report.setOsVersion(dto.getOsVersion());
    report.setTags(tags);
    report.markUpdatedAt();

    errorReportRepo.save(report);
  }

  @Transactional
  public void deleteErrorReport(UUID errorReportId, UUID currUserId) {
    ErrorReport report = checkAndGetErrorReport(errorReportId);

    if (!Objects.equals(report.getAuthor().getId(), currUserId)) {
      throw new InvalidOperationException("You cannot delete this error report.");
    }

    if (Objects.equals(report.getStatus(), ErrorReportStatus.RESOLVED)) {
      throw new InvalidOperationException("Cannot delete this error report, already resolved.");
    }

    List<UUID> tagIds = report.getTags().stream().map(Tag::getId).toList();
    if (!tagIds.isEmpty()) {
      tagRepo.decrementUsageCount(tagIds);
    }

    User author = userServiceImpl.checkAndGetUserByUserId(report.getAuthor().getId());
    List<Vote> votes =
        voteRepo.findAllByParentIdAndParentType(errorReportId, PostType.ERROR_REPORT);

    votes.forEach(
        vote -> {
          vote.getUser().setReputation(vote.getUser().getReputation() - 1);

          if (Objects.equals(vote.getVoteType(), VoteType.UPVOTE)) {
            author.setReputation(author.getReputation() - 1);
          } else {
            author.setReputation(author.getReputation() + 1);
          }

          voteRepo.save(vote);
        });
    author.setReputation(author.getReputation() - 4);

    errorReportRepo.delete(report);
  }
}
