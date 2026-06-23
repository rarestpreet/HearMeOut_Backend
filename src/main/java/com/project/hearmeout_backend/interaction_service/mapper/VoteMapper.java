package com.project.hearmeout_backend.interaction_service.mapper;

import com.project.hearmeout_backend.interaction_service.dto.request.VoteRequestDTO;
import com.project.hearmeout_backend.interaction_service.model.Vote;
import com.project.hearmeout_backend.interaction_service.model.enums.VoteType;
import com.project.hearmeout_backend.post_service.model.Post;
import com.project.hearmeout_backend.user_service.model.User;

public class VoteMapper {

  public static Vote toVoteEntity(
      VoteRequestDTO voteRequestDTO, User user, Post post, VoteType newVoteType) {
    return Vote.builder().user(user).post(post).voteType(newVoteType).build();
  }
}
