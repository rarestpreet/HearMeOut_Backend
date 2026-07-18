package com.project.hearmeout_backend.user_service.model;

import com.project.hearmeout_backend.authentication_service.model.enums.RoleType;
import com.project.hearmeout_backend.common_lib.model.BaseModel;
import com.project.hearmeout_backend.interaction_service.model.Comment;
import com.project.hearmeout_backend.interaction_service.model.Vote;
import com.project.hearmeout_backend.post_service.model.ErrorReport;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "user_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseModel {

  @Column(nullable = false, unique = true, length = 20)
  private String username;

  @Column(nullable = false, unique = true, length = 50)
  private String email;

  @Column(nullable = false)
  private String password;

  @Builder.Default private int reputation = 0;

  @Builder.Default private boolean isAccountVerified = false;

  @Builder.Default private boolean isAccountTerminated = false;

  private RoleType role;

  @Column(length = 100)
  private String fullName;

  @Column(length = 255)
  private String bio;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profession_id")
  private Profession profession;

  @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<ErrorReport> errorReports = new ArrayList<>();

  @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Comment> comments = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Vote> votes = new ArrayList<>();

  @Column(nullable = false)
  private LocalDateTime emailUpdatedAt;

  @Column(nullable = false)
  private LocalDateTime usernameUpdatedAt;

  @Override
  public void onCreate() {
    super.onCreate();
    this.usernameUpdatedAt = LocalDateTime.now();
    this.emailUpdatedAt = LocalDateTime.now();
  }

  public void markUpdatedAt(boolean isEmailUpdated, boolean isUsernameUpdated) {
    if (isEmailUpdated) {
      this.emailUpdatedAt = LocalDateTime.now();
    }
    if (isUsernameUpdated) {
      this.usernameUpdatedAt = LocalDateTime.now();
    }
  }

  public long emailUpdateCooldown() {
    return Math.max(
        0,
        7 - ChronoUnit.DAYS.between(this.emailUpdatedAt.toLocalDate(), java.time.LocalDate.now()));
  }

  public long usernameUpdateCooldown() {
    return Math.max(
        0,
        7
            - ChronoUnit.DAYS.between(
                this.usernameUpdatedAt.toLocalDate(), java.time.LocalDate.now()));
  }
}
