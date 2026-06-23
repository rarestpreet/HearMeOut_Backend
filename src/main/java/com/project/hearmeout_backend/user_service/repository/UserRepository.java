package com.project.hearmeout_backend.user_service.repository;

import com.project.hearmeout_backend.feed_service.dto.response.HomeUserProfileResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserDetailResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserProfileResponseDTO;
import com.project.hearmeout_backend.user_service.model.User;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@NullMarked
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByUsername(String username);

  @Query(
      """
                SELECT new com.project.hearmeout_backend.user_service.dto.response.UserDetailResponseDTO(
                    u.id, u.username, u.email, u.password, u.role
                )
                FROM User u
                WHERE u.email = :email
                AND u.isAccountTerminated = false
            """)
  Optional<UserDetailResponseDTO> findUserForAuth(@Param(value = "email") String email);

  @Query(
      """
            SELECT new com.project.hearmeout_backend.user_service.dto.response.UserProfileResponseDTO(
                u.id, u.username, u.email, u.reputation, u.createdAt, u.isAccountVerified, u.isAccountTerminated
            )
            FROM User u
            WHERE u.username = :username
            """)
  Optional<UserProfileResponseDTO> getUserProfileByUsername(@Param("username") String username);

  @Query(
      """
            SELECT new com.project.hearmeout_backend.feed_service.dto.response.HomeUserProfileResponseDTO(
                u.username, u.id, u.isAccountVerified, u.role
            )
            FROM User u
            WHERE u.id = :id
            """)
  Optional<HomeUserProfileResponseDTO> getHomeUserProfileById(@Param("id") Long id);

  boolean existsByUsernameOrEmail(String username, String email);

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);
}
