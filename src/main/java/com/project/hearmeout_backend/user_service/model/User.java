package com.project.hearmeout_backend.user_service.model;

import com.project.hearmeout_backend.authentication_service.model.enums.RoleType;
import com.project.hearmeout_backend.interaction_service.model.Comment;
import com.project.hearmeout_backend.interaction_service.model.Vote;
import com.project.hearmeout_backend.post_service.model.Post;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    private int reputation = 0;

    @Builder.Default
    private boolean isAccountVerified = false;

    @Builder.Default
    private boolean isAccountTerminated = false;

    @Builder.Default
    private List<RoleType> roles = new ArrayList<>(List.of(RoleType.USER));

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Vote> votes = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDate emailUpdatedAt;

    @Column(nullable = false)
    private LocalDate usernameUpdatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.usernameUpdatedAt = LocalDate.now();
        this.emailUpdatedAt = LocalDate.now();
    }

    public void markUpdatedAt(boolean isEmailUpdated, boolean isUsernameUpdated) {
        if (isEmailUpdated) {
            this.emailUpdatedAt = LocalDate.now();
        }
        if (isUsernameUpdated) {
            this.usernameUpdatedAt = LocalDate.now();
        }
    }

    public long emailUpdateCooldown() {
        return Math.max(
                0,
                7 - ChronoUnit.DAYS.between(
                        this.emailUpdatedAt,
                        LocalDate.now()
                )
        );
    }

    public long usernameUpdateCooldown() {
        return Math.max(
                0,
                7 - ChronoUnit.DAYS.between(
                        this.usernameUpdatedAt,
                        LocalDate.now()
                )
        );
    }
}