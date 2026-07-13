package com.project.hearmeout_backend.interaction_service.model;

import com.project.hearmeout_backend.gateway.model.BaseModel;
import com.project.hearmeout_backend.interaction_service.model.enums.CommentType;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.user_service.model.User;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseModel {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @Column(nullable = false)
  private UUID parentId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PostType parentType;

  @Column(nullable = false)
  private CommentType type;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String body;
}
