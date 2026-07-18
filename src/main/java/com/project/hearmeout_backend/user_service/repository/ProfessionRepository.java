package com.project.hearmeout_backend.user_service.repository;

import com.project.hearmeout_backend.user_service.model.Profession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessionRepository extends JpaRepository<Profession, UUID> {
  Optional<Profession> findByNameIgnoreCase(String name);

  @Query(
      """
            SELECT p.name
            FROM Profession p
            WHERE p.isApproved = true
            AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY p.usageCount DESC
        """)
  List<String> findTopValues(@Param("search") String search, Pageable pageable);
}
