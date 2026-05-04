
package com.project.hearmeout_backend.model;

import com.project.hearmeout_backend.model.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 20)
    private String password;

    @Builder.Default
    private int reputation = 0;

    private String emailVerifyOtp;
    private Long emailVerifyOtpExpireAt;
    private String passwordChangeOtp;
    private Long passwordOtpExpireAt;

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
}