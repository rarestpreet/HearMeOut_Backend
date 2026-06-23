package com.project.hearmeout_backend.post_service.mapper;

import com.project.hearmeout_backend.post_service.dto.request.AnswerSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.dto.request.QuestionSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.model.Post;
import com.project.hearmeout_backend.post_service.model.Tag;
import com.project.hearmeout_backend.post_service.model.enums.PostStatus;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
import com.project.hearmeout_backend.user_service.model.User;
import java.util.List;

public class PostMapper {

  public static Post answerToPostEntity(
      AnswerSubmitRequestDTO answerSubmitRequestDTO, Post parent, User author) {
    return Post.builder()
        .title(null)
        .body(answerSubmitRequestDTO.getBody())
        .postType(PostType.ANSWER)
        .parent(parent)
        .author(author)
        .status(PostStatus.UNREVIEWED)
        .build();
  }

  public static Post questionToPostEntity(
      QuestionSubmitRequestDTO questionSubmitRequestDTO, User author, List<Tag> tags) {
    return Post.builder()
        .title(questionSubmitRequestDTO.getTitle())
        .body(questionSubmitRequestDTO.getBody())
        .postType(PostType.QUESTION)
        .author(author)
        .tags(tags)
        .body(questionSubmitRequestDTO.getBody())
        .status(PostStatus.UNANSWERED)
        .build();
  }
}
