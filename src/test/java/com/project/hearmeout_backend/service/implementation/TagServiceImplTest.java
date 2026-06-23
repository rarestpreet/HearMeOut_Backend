package com.project.hearmeout_backend.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.hearmeout_backend.post_service.dto.request.TagCreationRequestDTO;
import com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO;
import com.project.hearmeout_backend.post_service.model.Tag;
import com.project.hearmeout_backend.post_service.repository.TagRepository;
import com.project.hearmeout_backend.post_service.service.implementation.TagServiceImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class TagServiceImplTest {

  @Mock private TagRepository tagRepo;

  @InjectMocks private TagServiceImpl tagService;

  @Test
  public void createTag() {
    // Arrange
    TagCreationRequestDTO tagDTO = new TagCreationRequestDTO();
    tagDTO.setDescription(
        "Tests whether a tag is created, validated, and saved correctly with expected data.");
    tagDTO.setName("Test");

    // Act
    tagService.createNewTag(tagDTO);

    // Assert
    ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
    verify(tagRepo).save(tagCaptor.capture());
    Tag savedTag = tagCaptor.getValue();

    assertEquals(tagDTO.getName(), savedTag.getName());
    assertEquals(tagDTO.getDescription(), savedTag.getDescription());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, -3})
  void getTags(int pageNum) {
    // Arrange
    List<TagResponseDTO> tagList =
        List.of(
            TagResponseDTO.builder().tagId(1L).name("Test1").build(),
            TagResponseDTO.builder().tagId(2L).name("Test2").build(),
            TagResponseDTO.builder().tagId(3L).name("Test3").build(),
            TagResponseDTO.builder().tagId(4L).name("Test4").build(),
            TagResponseDTO.builder().tagId(5L).name("Test5").build(),
            TagResponseDTO.builder().tagId(6L).name("Test6").build(),
            TagResponseDTO.builder().tagId(7L).name("Test7").build(),
            TagResponseDTO.builder().tagId(8L).name("Test8").build(),
            TagResponseDTO.builder().tagId(9L).name("Test9").build(),
            TagResponseDTO.builder().tagId(10L).name("Test10").build(),
            TagResponseDTO.builder().tagId(11L).name("Test11").build());

    pageNum = Math.max(0, pageNum);
    Pageable pageable = PageRequest.of(pageNum, 10);
    int start = Math.min(pageNum * 10, tagList.size());
    int end = Math.min((start + 10), tagList.size());

    when(tagRepo.findAllTagsDTO(pageable)).thenReturn(tagList.subList(start, end));

    // Act
    List<TagResponseDTO> result = tagService.getAllTags(pageNum);

    // Assert
    verify(tagRepo).findAllTagsDTO(pageable);

    assertNotNull(result);
    assertEquals(result.size(), end - start);
    assertEquals(tagList.subList(start, end), result);
    if (start < tagList.size()) {
      assertEquals(tagList.get(start).getTagId(), result.getFirst().getTagId());
    }
  }
}
