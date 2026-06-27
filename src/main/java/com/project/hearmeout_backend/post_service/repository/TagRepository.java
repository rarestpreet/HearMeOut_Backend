package com.project.hearmeout_backend.post_service.repository;

import com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO;
import com.project.hearmeout_backend.post_service.model.Tag;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@NullMarked
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

  @Query(
      """
          SELECT new com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO(
              t.id, t.name, t.description, t.usageCount
          )
          FROM Post p
          JOIN p.tags t
          WHERE p.id = :postId
      """)
  List<TagResponseDTO> findTagsDTOByPostId(@Param("postId") Long postId);

  @Query(
      """
          SELECT new com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO(
              t.id, t.name, t.description, t.usageCount
          )
          FROM Tag t
      """)
  List<TagResponseDTO> findAllTagsDTO(Pageable pageable);

  @Modifying
  @Query(
      """
         UPDATE Tag t
         SET t.usageCount = t.usageCount + 1
        WHERE t.id IN :tagIds
      """)
  void incrementUsageCount(@Param("tagIds") List<Long> tagIds);

  @Modifying
  @Query(
      """
         UPDATE Tag t
         SET t.usageCount = t.usageCount - 1
        WHERE t.id IN :tagIds
      """)
  void decrementUsageCount(@Param("tagIds") List<Long> tagIds);
}
