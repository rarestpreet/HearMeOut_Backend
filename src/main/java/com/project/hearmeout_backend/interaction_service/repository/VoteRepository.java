package com.project.hearmeout_backend.interaction_service.repository;

import com.project.hearmeout_backend.interaction_service.model.Vote;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@NullMarked
@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {

  Optional<Vote> findByParentIdAndParentTypeAndUserId(
      UUID parentId, PostType parentType, UUID userId);

  boolean existsByParentIdAndParentTypeAndUserId(UUID parentId, PostType parentType, UUID userId);

  void deleteByParentIdAndParentTypeAndUserId(UUID parentId, PostType parentType, UUID userId);

  List<Vote> findAllByParentIdAndParentType(UUID parentId, PostType parentType);
}
