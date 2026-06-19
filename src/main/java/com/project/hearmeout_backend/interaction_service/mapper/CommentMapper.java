package com.project.hearmeout_backend.interaction_service.mapper;

import com.project.hearmeout_backend.interaction_service.dto.request.CommentRequestDTO;
import com.project.hearmeout_backend.interaction_service.model.Comment;
import com.project.hearmeout_backend.post_service.model.Post;
import com.project.hearmeout_backend.user_service.model.User;

public class CommentMapper {

    public static Comment toCommentEntity(CommentRequestDTO commentRequestDTO, Post post, User author) {
        return Comment.builder()
                .post(post)
                .author(author)
                .body(commentRequestDTO.getBody())
                .build();
    }
}
