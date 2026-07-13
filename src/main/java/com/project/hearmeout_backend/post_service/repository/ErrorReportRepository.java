package com.project.hearmeout_backend.post_service.repository;

import com.project.hearmeout_backend.feed_service.dto.response.FeedErrorReportResponseDTO;
import com.project.hearmeout_backend.post_service.dto.response.ErrorReportResponseDTO;
import com.project.hearmeout_backend.post_service.model.ErrorReport;
import com.project.hearmeout_backend.user_service.dto.response.UserErrorReportResponseDTO;
import java.util.Optional;
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
public interface ErrorReportRepository extends JpaRepository<ErrorReport, UUID> {

  @Query(
      """
          SELECT new com.project.hearmeout_backend.user_service.dto.response.UserErrorReportResponseDTO(
              e.id, e.title, e.score, e.updatedAt, e.status
          )
          FROM ErrorReport e
          WHERE e.author.username = :username
      """)
  Page<UserErrorReportResponseDTO> findUserErrorReportsByUsername(
      @Param("username") String username, Pageable pageable);

  @Query(
      """
          SELECT new com.project.hearmeout_backend.feed_service.dto.response.FeedErrorReportResponseDTO(
              e.id, e.author.username, e.title, e.score, e.updatedAt, e.status, e.viewCount
          )
          FROM ErrorReport e
          WHERE e.author.id != :userId
      """)
  Page<FeedErrorReportResponseDTO> findFeedErrorReportsByAuthorIdNot(
      @Param("userId") UUID userId, Pageable pageable);

  @Query(
      """
          SELECT new com.project.hearmeout_backend.feed_service.dto.response.FeedErrorReportResponseDTO(
              e.id, e.author.username, e.title, e.score, e.updatedAt, e.status, e.viewCount
          )
          FROM ErrorReport e
      """)
  Page<FeedErrorReportResponseDTO> findFeedErrorReports(Pageable pageable);

  @Query(
      """
          SELECT new com.project.hearmeout_backend.post_service.dto.response.ErrorReportResponseDTO(
              e.id, e.author.username, e.title, e.description, e.score, e.updatedAt, e.status
          )
          FROM ErrorReport e
          WHERE e.id = :errorReportId
      """)
  Optional<ErrorReportResponseDTO> findErrorReportDetailsDTO(
      @Param("errorReportId") UUID errorReportId);
}
