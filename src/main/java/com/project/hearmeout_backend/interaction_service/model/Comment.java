package com.project.hearmeout_backend.interaction_service.model;

import com.project.hearmeout_backend.gateway.model.BaseModel;
import com.project.hearmeout_backend.post_service.model.Post;
import com.project.hearmeout_backend.user_service.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseModel {

    @Lob
    @Column(nullable = false, length = 200)
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}