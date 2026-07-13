package com.project.hearmeout_backend.interaction_service.mapper;

import com.project.hearmeout_backend.interaction_service.model.Vote;
import com.project.hearmeout_backend.interaction_service.model.enums.VoteType;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.user_service.model.User;
import java.util.UUID;

public class VoteMapper {

  public static Vote toVoteEntity(
      UUID parentId, PostType parentType, User user, VoteType voteType) {
    return Vote.builder()
        .user(user)
        .parentId(parentId)
        .parentType(parentType)
        .voteType(voteType)
        .build();
  }
}
