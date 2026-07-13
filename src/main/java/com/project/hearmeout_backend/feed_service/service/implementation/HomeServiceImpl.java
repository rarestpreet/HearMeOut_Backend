package com.project.hearmeout_backend.feed_service.service.implementation;

import com.project.hearmeout_backend.common_lib.dto.PageData;
import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.feed_service.dto.response.FeedErrorReportResponseDTO;
import com.project.hearmeout_backend.feed_service.dto.response.HomeUserProfileResponseDTO;
import com.project.hearmeout_backend.post_service.dto.response.ReportTagResponseDTO;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.post_service.repository.ErrorReportRepository;
import com.project.hearmeout_backend.post_service.repository.TagRepository;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl {

  private final ErrorReportRepository postRepo;
  private final TagRepository tagRepo;
  private final UserRepository userRepo;

  @Transactional(readOnly = true)
  public PagedResponse<FeedErrorReportResponseDTO> generateFeed(int limit, int offset, Long userId) {
    int page = offset / limit;
    Pageable pageable = PageRequest.of(Math.max(page, 0), limit);
    Page<FeedErrorReportResponseDTO> feedPostsPage;

    if (userId != null)
      feedPostsPage =
          postRepo.findFeedErrorReportDTOByAuthorIdNot(PostType.ERROR_REPORT, userId, pageable);
    else feedPostsPage = postRepo.findFeedErrorReportDTO(PostType.ERROR_REPORT, pageable);

    List<FeedErrorReportResponseDTO> feedPosts =
        feedPostsPage.getContent().stream()
            .map(
                post -> {
                  List<ReportTagResponseDTO> tags =
                      tagRepo.findTagsDTOByPostId(post.getNavigationId());

                  return FeedErrorReportResponseDTO.builder()
                      .updatedAt(post.getUpdatedAt())
                      .score(post.getScore())
                      .status(post.getStatus())
                      .title(post.getTitle())
                      .tags(tags)
                      .navigationId(post.getNavigationId())
                      .authorUsername(post.getAuthorUsername())
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
  public HomeUserProfileResponseDTO getUserProfile(Long userId) {
    if (userId == null) {
      return HomeUserProfileResponseDTO.builder()
          .username(null)
          .userNavigationId(null)
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
