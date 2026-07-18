package com.project.hearmeout_backend.post_service.repository;

import com.project.hearmeout_backend.post_service.model.ProgrammingLanguage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgrammingLanguageRepository extends JpaRepository<ProgrammingLanguage, UUID> {
  Optional<ProgrammingLanguage> findByNameIgnoreCase(String name);

  @Query(
      """
            SELECT l.name
            FROM ProgrammingLanguage l
            WHERE l.isApproved = true
            AND (:search IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY l.usageCount DESC
        """)
  List<String> findTopValues(@Param("search") String search, Pageable pageable);
}
