package com.project.hearmeout_backend.post_service.model;

import com.project.hearmeout_backend.gateway.model.BaseModel;
import com.project.hearmeout_backend.post_service.model.enums.PostStatus;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.interaction_service.model.Comment;
import com.project.hearmeout_backend.interaction_service.model.Vote;
import com.project.hearmeout_backend.user_service.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseModel {

    /** Only QUESTION posts have a title */
    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType postType;

    /** For ANSWER posts: points to the parent QUESTION */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Post parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Post> answers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /** Cached vote score — updated on every vote action */
    @Builder.Default
    private int score = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Vote> votes = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tag_used",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();
}