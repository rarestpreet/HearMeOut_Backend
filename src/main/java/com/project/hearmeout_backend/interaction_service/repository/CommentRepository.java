package com.project.hearmeout_backend.interaction_service.repository;

import com.project.hearmeout_backend.interaction_service.dto.response.CommentResponseDTO;
import com.project.hearmeout_backend.interaction_service.model.Comment;
import com.project.hearmeout_backend.user_service.dto.response.UserCommentResponseDTO;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@NullMarked
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

  @Query(
      """
            SELECT new com.project.hearmeout_backend.user_service.dto.response.UserCommentResponseDTO(
                    c.body, COALESCE(p.parent.id, p.id), p.body, c.updatedAt
                    )
            FROM Comment c
            JOIN c.post p
            WHERE c.author.username = :username
            """)
  Page<UserCommentResponseDTO> findUserCommentsByUsername(
      @Param("username") String username, Pageable pageable);

  @Query(
      """
        SELECT new com.project.hearmeout_backend.interaction_service.dto.response.CommentResponseDTO(
            c.id, c.body, c.author.username, p.id, c.updatedAt
        )
        FROM Comment c
        JOIN c.post p
        WHERE c.post.id = :postId
    """)
  Page<CommentResponseDTO> findCommentsDTOByPostId(@Param("postId") Long postId, Pageable pageable);
}
