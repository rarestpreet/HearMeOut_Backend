package com.project.hearmeout_backend.post_service.repository;

import com.project.hearmeout_backend.post_service.model.OperatingSystem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OperatingSystemRepository extends JpaRepository<OperatingSystem, UUID> {
  Optional<OperatingSystem> findByNameIgnoreCase(String name);

  @Query(
      """
            SELECT o.name
            FROM OperatingSystem o
            WHERE o.isApproved = true
            AND (:search IS NULL OR LOWER(o.name) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY o.usageCount DESC
        """)
  List<String> findTopValues(@Param("search") String search, Pageable pageable);
}
