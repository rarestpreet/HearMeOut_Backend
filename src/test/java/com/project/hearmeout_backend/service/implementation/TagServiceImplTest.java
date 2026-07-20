package com.project.hearmeout_backend.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO;
import com.project.hearmeout_backend.post_service.repository.TagRepository;
import com.project.hearmeout_backend.post_service.service.implementation.TagServiceImpl;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class TagServiceImplTest {

  @Mock private TagRepository tagRepo;

  @InjectMocks private TagServiceImpl tagService;

  @ParameterizedTest
  @CsvSource({"10, 0", "10, 10", "10, 20", "5, 5"})
  void getAllTagsTest(int limit, int offset) {
    // Arrange
    List<TagResponseDTO> tagList =
        List.of(
            TagResponseDTO.builder().tagId(UUID.randomUUID()).name("Test1").usageCount(3).build(),
            TagResponseDTO.builder().tagId(UUID.randomUUID()).name("Test2").usageCount(0).build(),
            TagResponseDTO.builder().tagId(UUID.randomUUID()).name("Test3").usageCount(5).build(),
            TagResponseDTO.builder().tagId(UUID.randomUUID()).name("Test4").usageCount(1).build(),
            TagResponseDTO.builder().tagId(UUID.randomUUID()).name("Test5").usageCount(0).build(),
            TagResponseDTO.builder().tagId(UUID.randomUUID()).name("Test6").usageCount(2).build(),
            TagResponseDTO.builder().tagId(UUID.randomUUID()).name("Test7").usageCount(0).build(),
            TagResponseDTO.builder().tagId(UUID.randomUUID()).name("Test8").usageCount(7).build(),
            TagResponseDTO.builder().tagId(UUID.randomUUID()).name("Test9").usageCount(0).build(),
            TagResponseDTO.builder().tagId(UUID.randomUUID()).name("Test10").usageCount(4).build(),
            TagResponseDTO.builder().tagId(UUID.randomUUID()).name("Test11").usageCount(1).build());

    int page = offset / limit;
    Pageable pageable = PageRequest.of(Math.max(page, 0), limit);
    int start = Math.min(Math.max(page, 0) * limit, tagList.size());
    int end = Math.min((start + limit), tagList.size());

    List<TagResponseDTO> pagedTags = tagList.subList(start, end);
    Page<TagResponseDTO> tagPage = new PageImpl<>(pagedTags, pageable, tagList.size());

    when(tagRepo.findAllTagsDTO(pageable)).thenReturn(tagPage);

    // Act
    PagedResponse<TagResponseDTO> result = tagService.getAllTags(limit, offset);

    // Assert
    verify(tagRepo).findAllTagsDTO(pageable);

    assertNotNull(result);
    assertEquals(result.getData().size(), end - start);
    assertEquals(pagedTags, result.getData());
    if (start < tagList.size()) {
      assertEquals(pagedTags.getFirst().getTagId(), result.getData().getFirst().getTagId());
      assertNotNull(result.getData().getFirst().getUsageCount());
    }
    assertEquals(tagList.size(), result.getPageData().getTotal());
    assertEquals(limit, result.getPageData().getLimit());
    assertEquals(offset, result.getPageData().getOffset());
    assertEquals(tagPage.hasNext(), result.getPageData().isHasMore());
  }
}
