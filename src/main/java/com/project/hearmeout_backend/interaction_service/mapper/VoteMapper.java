package com.project.hearmeout_backend.interaction_service.mapper;

import com.project.hearmeout_backend.interaction_service.dto.request.VoteRequestDTO;
import com.project.hearmeout_backend.post_service.model.Post;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.interaction_service.model.Vote;

public class VoteMapper {

    public static Vote toVoteEntity(VoteRequestDTO voteRequestDTO, User user, Post post) {
        return Vote.builder()
                .user(user)
                .post(post)
                .voteType(voteRequestDTO.getVoteType())
                .build();
    }
}
