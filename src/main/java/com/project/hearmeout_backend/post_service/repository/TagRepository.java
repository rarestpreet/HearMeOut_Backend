package com.project.hearmeout_backend.post_service.repository;

import com.project.hearmeout_backend.post_service.dto.response.ReportTagResponseDTO;
import com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO;
import com.project.hearmeout_backend.post_service.model.Tag;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@NullMarked
@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

  @Query(
      """
          SELECT new com.project.hearmeout_backend.post_service.dto.response.ReportTagResponseDTO(
              t.id, t.name, t.description
          )
          FROM ErrorReport e
          JOIN e.tags t
          WHERE e.id = :errorReportId
      """)
  List<ReportTagResponseDTO> findTagsByErrorReportId(@Param("errorReportId") UUID errorReportId);

  @Query(
      """
          SELECT new com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO(
              t.id, t.name, t.description, t.usageCount
          )
          FROM Tag t
      """)
  Page<TagResponseDTO> findAllTagsDTO(Pageable pageable);

  @Modifying
  @Query(
      """
         UPDATE Tag t
         SET t.usageCount = t.usageCount + 1
        WHERE t.id IN :tagIds
      """)
  void incrementUsageCount(@Param("tagIds") List<UUID> tagIds);

  @Modifying
  @Query(
      """
         UPDATE Tag t
         SET t.usageCount = t.usageCount - 1
        WHERE t.id IN :tagIds
      """)
  void decrementUsageCount(@Param("tagIds") List<UUID> tagIds);
}
