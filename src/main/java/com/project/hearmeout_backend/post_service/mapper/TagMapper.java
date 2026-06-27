package com.project.hearmeout_backend.post_service.mapper;

import com.project.hearmeout_backend.post_service.dto.request.TagCreationRequestDTO;
import com.project.hearmeout_backend.post_service.model.Tag;
import jakarta.validation.Valid;

public class TagMapper {

  public static Tag toTagEntity(TagCreationRequestDTO tag) {
    return Tag.builder().name(tag.getName()).description(tag.getDescription()).build();
  }

  public static void applyModification(
      Tag tag, @Valid TagCreationRequestDTO tagModificationRequestDTO) {
    tag.setDescription(tagModificationRequestDTO.getDescription());
    tag.setName(tagModificationRequestDTO.getName());
  }
}
