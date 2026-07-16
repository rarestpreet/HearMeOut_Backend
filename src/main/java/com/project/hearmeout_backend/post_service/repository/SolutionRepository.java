package com.project.hearmeout_backend.post_service.repository;

import com.project.hearmeout_backend.post_service.dto.response.SolutionResponseDTO;
import com.project.hearmeout_backend.post_service.model.Solution;
import com.project.hearmeout_backend.user_service.dto.response.UserAnswerResponseDTO;
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
public interface SolutionRepository extends JpaRepository<Solution, UUID> {

  @Query(
      """
          SELECT new com.project.hearmeout_backend.user_service.dto.response.UserAnswerResponseDTO(
              s.explanation, s.status, s.score, s.updatedAt, s.errorReport.id, s.errorReport.title
          )
          FROM Solution s
          WHERE s.author.username = :username
      """)
  Page<UserAnswerResponseDTO> findUserSolutionsByUsername(
      @Param("username") String username, Pageable pageable);

  @Query(
      """
          SELECT new com.project.hearmeout_backend.post_service.dto.response.SolutionResponseDTO(
              s.id, s.author.username, s.explanation, s.score, s.updatedAt, s.status,
              s.language, s.languageVersion, s.framework, s.frameworkVersion, s.os, s.osVersion,
              s.probableCause, s.codeChange
          )
          FROM Solution s
          WHERE s.errorReport.id = :errorReportId
      """)
  Page<SolutionResponseDTO> findSolutionsByErrorReportId(
      @Param("errorReportId") UUID errorReportId, Pageable pageable);
}
