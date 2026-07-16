package com.project.hearmeout_backend.interaction_service.repository;

import com.project.hearmeout_backend.interaction_service.dto.response.CommentResponseDTO;
import com.project.hearmeout_backend.interaction_service.model.Comment;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.user_service.dto.response.UserCommentResponseDTO;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@NullMarked
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

  @Query(
      """
          SELECT new com.project.hearmeout_backend.user_service.dto.response.UserCommentResponseDTO(
              c.body, c.parentId, c.parentType, c.updatedAt
          )
          FROM Comment c
          WHERE c.author.username = :username
      """)
  Page<UserCommentResponseDTO> findUserCommentsByUsername(
      @Param("username") String username, Pageable pageable);

  @Query(
      """
          SELECT new com.project.hearmeout_backend.interaction_service.dto.response.CommentResponseDTO(
              c.id, c.body, c.author.username, c.parentId, c.updatedAt, c.type
          )
          FROM Comment c
          WHERE c.parentId = :parentId AND c.parentType = :parentType
      """)
  Page<CommentResponseDTO> findCommentsByParent(
      @Param("parentId") UUID parentId,
      @Param("parentType") PostType parentType,
      Pageable pageable);
}
