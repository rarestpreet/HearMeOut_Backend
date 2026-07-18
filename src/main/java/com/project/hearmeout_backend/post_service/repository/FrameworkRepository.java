package com.project.hearmeout_backend.post_service.repository;

import com.project.hearmeout_backend.post_service.model.Framework;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FrameworkRepository extends JpaRepository<Framework, UUID> {
  Optional<Framework> findByNameIgnoreCase(String name);

  @Query(
      """
            SELECT f.name
            FROM Framework f
            WHERE f.isApproved = true
            AND (:search IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY f.usageCount DESC
        """)
  List<String> findTopValues(@Param("search") String search, Pageable pageable);
}
