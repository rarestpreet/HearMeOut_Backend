package com.project.hearmeout_backend.service.implementation;

import com.project.hearmeout_backend.dto.response.post_response.FeedQuestionResponseDTO;
import com.project.hearmeout_backend.dto.response.tag_response.TagResponseDTO;
import com.project.hearmeout_backend.dto.response.user_response.HomeUserProfileResponseDTO;
import com.project.hearmeout_backend.exception.UserNotFoundException;
import com.project.hearmeout_backend.model.enums.PostType;
import com.project.hearmeout_backend.repository.PostRepository;
import com.project.hearmeout_backend.repository.TagRepository;
import com.project.hearmeout_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl {

    private final PostRepository postRepo;
    private final TagRepository tagRepo;
    private final UserRepository userRepo;

    @Transactional(readOnly = true)
    public List<FeedQuestionResponseDTO> generateFeed(int pageNum, Long userId) {
        Pageable pageable = PageRequest.of(
                Math.max(pageNum, 0), 10
        );
        List<FeedQuestionResponseDTO> feedPosts;

        if (userId != null)
            feedPosts = postRepo.findFeedPostsDTOByPostTypeAndAuthorIdNot(PostType.QUESTION, userId, pageable);
        else
            feedPosts = postRepo.findFeedPostsDTOByPostType(PostType.QUESTION, pageable);

        feedPosts.forEach(post -> {
            List<TagResponseDTO> tags = tagRepo.findTagsDTOByPostId(post.getNavigationPostId());
            post.setTags(tags);
        });

        return feedPosts;
    }

    @Transactional(readOnly = true)
    public HomeUserProfileResponseDTO getUserProfile(Long userId) {
        if (userId == null) {
            return new HomeUserProfileResponseDTO(null, null, false, new ArrayList<>());
        }

        return userRepo.getHomeUserProfileById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id:  " + userId + " was not found")
                );
    }
}
