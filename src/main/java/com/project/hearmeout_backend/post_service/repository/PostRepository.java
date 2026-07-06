package com.project.hearmeout_backend.post_service.repository;

import com.project.hearmeout_backend.feed_service.dto.response.FeedQuestionResponseDTO;
import com.project.hearmeout_backend.post_service.dto.response.PostAnswerResponseDTO;
import com.project.hearmeout_backend.post_service.dto.response.QuestionPostResponseDTO;
import com.project.hearmeout_backend.post_service.model.Post;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.user_service.dto.response.UserAnswerResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserQuestionResponseDTO;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@NullMarked
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  @Query(
      """
                SELECT new com.project.hearmeout_backend.user_service.dto.response.UserAnswerResponseDTO(
                a.body, a.status, a.score, a.updatedAt, q.id, q.title
                )
                FROM Post a
                JOIN a.parent q
                WHERE a.author.username = :username
                AND a.postType = :postType
            """)
  Page<UserAnswerResponseDTO> findUserAnswerByUsername(
      @Param("username") String username, @Param("postType") PostType postType, Pageable pageable);

  @Query(
      """
                SELECT new com.project.hearmeout_backend.user_service.dto.response.UserQuestionResponseDTO(
                q.id, q.title, q.score, q.updatedAt, q.status
                )
                FROM Post q
                WHERE q.author.username = :username
                AND q.postType = :postType
            """)
  Page<UserQuestionResponseDTO> findUserQuestionByUsername(
      @Param("username") String username, @Param("postType") PostType postType, Pageable pageable);

  @Query(
      """
        SELECT new com.project.hearmeout_backend.feed_service.dto.response.FeedQuestionResponseDTO(
            p.id, p.author.username, p.title, p.score, p.updatedAt, p.status
        )
        FROM Post p
        WHERE p.postType = :postType
        AND p.author.id != :userId
    """)
  Page<FeedQuestionResponseDTO> findFeedPostsDTOByPostTypeAndAuthorIdNot(
      @Param("postType") PostType postType, @Param("userId") Long userId, Pageable pageable);

  @Query(
      """
        SELECT new com.project.hearmeout_backend.feed_service.dto.response.FeedQuestionResponseDTO(
            p.id, p.author.username, p.title, p.score, p.updatedAt, p.status
        )
        FROM Post p
        WHERE p.postType = :postType
    """)
  Page<FeedQuestionResponseDTO> findFeedPostsDTOByPostType(
      @Param("postType") PostType postType, Pageable pageable);

  @Query(
      """
        SELECT new com.project.hearmeout_backend.post_service.dto.response.QuestionPostResponseDTO(
            p.id, p.author.username, p.title, p.body, p.score, p.updatedAt, p.status
        )
        FROM Post p
        WHERE p.id = :postId
        AND p.postType = :postType
    """)
  Optional<QuestionPostResponseDTO> findQuestionPostDetailsDTO(
      @Param("postId") Long postId, @Param("postType") PostType postType);

  @Query(
      """
        SELECT new com.project.hearmeout_backend.post_service.dto.response.PostAnswerResponseDTO(
            a.id, a.author.username, a.body, a.score, a.updatedAt, a.status
        )
        FROM Post a
        WHERE a.parent.id = :questionId AND a.postType = :postType
    """)
  Page<PostAnswerResponseDTO> findAnswersDTOByQuestionId(
      @Param("questionId") Long questionId,
      @Param("postType") PostType postType,
      Pageable pageable);
}
