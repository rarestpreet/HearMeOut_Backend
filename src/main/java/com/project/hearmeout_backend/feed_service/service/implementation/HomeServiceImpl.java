package com.project.hearmeout_backend.feed_service.service.implementation;

import com.project.hearmeout_backend.common_lib.dto.PageData;
import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.feed_service.dto.response.FeedErrorReportResponseDTO;
import com.project.hearmeout_backend.feed_service.dto.response.HomeUserProfileResponseDTO;
import com.project.hearmeout_backend.post_service.dto.response.ReportTagResponseDTO;
import com.project.hearmeout_backend.post_service.repository.ErrorReportRepository;
import com.project.hearmeout_backend.post_service.repository.TagRepository;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl {

  private final ErrorReportRepository errorReportRepo;
  private final TagRepository tagRepo;
  private final UserRepository userRepo;

  @Transactional(readOnly = true)
  public PagedResponse<FeedErrorReportResponseDTO> generateFeed(
      int limit, int offset, UUID userId) {
    int page = offset / limit;
    Pageable pageable = PageRequest.of(Math.max(page, 0), limit);
    Page<FeedErrorReportResponseDTO> feedPostsPage;

    if (userId != null)
      feedPostsPage = errorReportRepo.findFeedErrorReportsByAuthorIdNot(userId, pageable);
    else feedPostsPage = errorReportRepo.findFeedErrorReports(pageable);

    List<FeedErrorReportResponseDTO> feedPosts =
        feedPostsPage.getContent().stream()
            .map(
                post -> {
                  List<ReportTagResponseDTO> tags =
                      tagRepo.findTagsByErrorReportId(post.getNavigationId());

                  return FeedErrorReportResponseDTO.builder()
                      .updatedAt(post.getUpdatedAt())
                      .score(post.getScore())
                      .status(post.getStatus())
                      .title(post.getTitle())
                      .description(post.getDescription())
                      .tags(tags)
                      .navigationId(post.getNavigationId())
                      .authorUsername(post.getAuthorUsername())
                      .viewCount(post.getViewCount())
                      .build();
                })
            .toList();

    return PagedResponse.<FeedErrorReportResponseDTO>builder()
        .data(feedPosts)
        .pageData(
            PageData.builder()
                .hasMore(feedPostsPage.hasNext())
                .total(feedPostsPage.getTotalElements())
                .offset(offset)
                .limit(limit)
                .build())
        .build();
  }

  @Transactional(readOnly = true)
  public HomeUserProfileResponseDTO getUserProfile(UUID userId) {
    if (userId == null) {
      return HomeUserProfileResponseDTO.builder()
          .username(null)
          .navigationId(null)
          .accountVerified(false)
          .role(null)
          .build();
    }

    return userRepo
        .getHomeUserProfileById(userId)
        .orElseThrow(
            () -> new UserNotFoundException("User with id:  " + userId + " was not found"));
  }
}
