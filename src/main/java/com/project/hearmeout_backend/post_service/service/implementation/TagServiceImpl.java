package com.project.hearmeout_backend.post_service.service.implementation;

import com.project.hearmeout_backend.common_lib.dto.PageData;
import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.common_lib.exception.TagNotFoundException;
import com.project.hearmeout_backend.post_service.dto.request.TagCreationRequestDTO;
import com.project.hearmeout_backend.post_service.dto.request.TagModificationRequestDTO;
import com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO;
import com.project.hearmeout_backend.post_service.mapper.TagMapper;
import com.project.hearmeout_backend.post_service.model.Tag;
import com.project.hearmeout_backend.post_service.repository.TagRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagServiceImpl {

  private final TagRepository tagRepo;

  @Transactional(readOnly = true)
  public PagedResponse<TagResponseDTO> getAllTags(int limit, int offset) {
    int page = offset / limit;
    Pageable pageable = PageRequest.of(Math.max(page, 0), limit);

    Page<TagResponseDTO> tagPage = tagRepo.findAllTagsDTO(pageable);
    return PagedResponse.<TagResponseDTO>builder()
        .data(tagPage.getContent())
        .pageData(
            PageData.builder()
                .hasMore(tagPage.hasNext())
                .total(tagPage.getTotalElements())
                .offset(offset)
                .limit(limit)
                .build())
        .build();
  }

  @Transactional
  public void createNewTag(TagCreationRequestDTO tag) {
    tagRepo.save(TagMapper.toTagEntity(tag));
  }

  @Transactional
  public void updateTag(UUID tagId, TagModificationRequestDTO tagModificationRequestDTO) {
    Tag tag =
        tagRepo
            .findById(tagId)
            .orElseThrow(() -> new TagNotFoundException("Tag with id " + tagId + " not found"));

    tag.setDescription(tagModificationRequestDTO.getDescription());
    tag.markUpdatedAt();
    tagRepo.save(tag);
  }

  @Transactional
  public void deleteTag(UUID tagId) {
    if (!tagRepo.existsById(tagId)) {
      throw new TagNotFoundException("Tag with id " + tagId + " not found");
    }

    tagRepo.deleteById(tagId);
  }
}
