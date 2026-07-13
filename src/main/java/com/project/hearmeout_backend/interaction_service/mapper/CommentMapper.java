package com.project.hearmeout_backend.interaction_service.mapper;

import com.project.hearmeout_backend.interaction_service.dto.request.CommentRequestDTO;
import com.project.hearmeout_backend.interaction_service.model.Comment;
import com.project.hearmeout_backend.user_service.model.User;

public class CommentMapper {

  public static Comment toCommentEntity(CommentRequestDTO dto, User author) {
    return Comment.builder()
        .author(author)
        .parentId(dto.getParentId())
        .parentType(dto.getParentType())
        .type(dto.getCommentType())
        .body(dto.getBody())
        .build();
  }
}
