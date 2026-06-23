package com.project.hearmeout_backend.feed_service.service.implementation;

import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.feed_service.dto.response.FeedQuestionResponseDTO;
import com.project.hearmeout_backend.feed_service.dto.response.HomeUserProfileResponseDTO;
import com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.post_service.repository.PostRepository;
import com.project.hearmeout_backend.post_service.repository.TagRepository;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl {

  private final PostRepository postRepo;
  private final TagRepository tagRepo;
  private final UserRepository userRepo;

  @Transactional(readOnly = true)
  public List<FeedQuestionResponseDTO> generateFeed(int pageNum, Long userId) {
    Pageable pageable = PageRequest.of(Math.max(pageNum, 0), 10);
    List<FeedQuestionResponseDTO> feedPosts;

    if (userId != null)
      feedPosts =
          postRepo.findFeedPostsDTOByPostTypeAndAuthorIdNot(PostType.QUESTION, userId, pageable);
    else feedPosts = postRepo.findFeedPostsDTOByPostType(PostType.QUESTION, pageable);

    feedPosts =
        feedPosts.stream()
            .map(
                post -> {
                  List<TagResponseDTO> tags =
                      tagRepo.findTagsDTOByPostId(post.getNavigationPostId());

                  return FeedQuestionResponseDTO.builder()
                      .updatedAt(post.getUpdatedAt())
                      .score(post.getScore())
                      .postStatus(post.getPostStatus())
                      .title(post.getTitle())
                      .tags(tags)
                      .navigationPostId(post.getNavigationPostId())
                      .authorUsername(post.getAuthorUsername())
                      .build();
                })
            .toList();

    return feedPosts;
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
