package com.project.hearmeout_backend.post_service.service.implementation;

import com.project.hearmeout_backend.common_lib.dto.PageData;
import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO;
import com.project.hearmeout_backend.post_service.repository.TagRepository;
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
}
