package com.project.hearmeout_backend.post_service.service.implementation;

import com.project.hearmeout_backend.common_lib.exception.TagNotFoundException;
import com.project.hearmeout_backend.post_service.dto.request.TagCreationRequestDTO;
import com.project.hearmeout_backend.post_service.dto.request.TagModificationRequestDTO;
import com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO;
import com.project.hearmeout_backend.post_service.mapper.TagMapper;
import com.project.hearmeout_backend.post_service.model.Tag;
import com.project.hearmeout_backend.post_service.repository.TagRepository;
import com.project.hearmeout_backend.post_service.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

  private final TagRepository tagRepo;

  @Transactional(readOnly = true)
  public List<TagResponseDTO> getAllTags(int pageNum) {
    Pageable pageable = PageRequest.of(Math.max(pageNum, 0), 10);

    return tagRepo.findAllTagsDTO(pageable);
  }

  @Transactional
  public void createNewTag(TagCreationRequestDTO tag) {
    tagRepo.save(TagMapper.toTagEntity(tag));
  }

  @Transactional
  public void updateTag(Long tagId, TagModificationRequestDTO tagModificationRequestDTO) {
    Tag tag =
        tagRepo
            .findById(tagId)
            .orElseThrow(() -> new TagNotFoundException("Tag with id " + tagId + " not found"));

    tag.setDescription(tagModificationRequestDTO.getDescription());
    tag.markUpdatedAt();
    tagRepo.save(tag);
  }

  @Transactional
  public void deleteTag(Long tagId) {
    if (!tagRepo.existsById(tagId)) {
      throw new TagNotFoundException("Tag with id " + tagId + " not found");
    }

    tagRepo.deleteById(tagId);
  }
}
