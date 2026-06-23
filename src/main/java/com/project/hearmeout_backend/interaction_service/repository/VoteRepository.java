package com.project.hearmeout_backend.interaction_service.repository;

import com.project.hearmeout_backend.interaction_service.model.Vote;
import com.project.hearmeout_backend.post_service.model.Post;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@NullMarked
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
  boolean existsByPostIdAndUserId(Long postId, Long userId);

  Optional<Vote> findByPostIdAndUserId(Long postId, Long userId);

  void removeVoteByPostIdAndUserId(Long postId, Long userId);

  List<Vote> post(Post post);

  List<Vote> findAllByPostId(Long postId);
}
