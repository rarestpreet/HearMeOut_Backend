package com.project.hearmeout_backend.interaction_service.model;

import com.project.hearmeout_backend.common_lib.model.BaseModel;
import com.project.hearmeout_backend.interaction_service.model.enums.VoteType;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.user_service.model.User;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(
    name = "vote",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_vote_user_parent",
            columnNames = {"user_id", "parent_id", "parent_type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vote extends BaseModel {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private UUID parentId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PostType parentType;

  @Column(nullable = false)
  private VoteType voteType;
}
