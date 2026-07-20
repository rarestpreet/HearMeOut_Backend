package com.project.hearmeout_backend.administration_service.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.project.hearmeout_backend.administration_service.dto.request.TagCreationRequestDTO;
import com.project.hearmeout_backend.administration_service.dto.request.TagModificationRequestDTO;
import com.project.hearmeout_backend.common_lib.exception.TagNotFoundException;
import com.project.hearmeout_backend.post_service.model.Tag;
import com.project.hearmeout_backend.post_service.repository.TagRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AdminTagServiceImplTest {

  @Mock private TagRepository tagRepo;

  @InjectMocks private AdminTagServiceImpl adminTagService;

  @Test
  public void createTag() {
    // Arrange
    TagCreationRequestDTO tagDTO = new TagCreationRequestDTO();
    tagDTO.setDescription(
        "Tests whether a tag is created, validated, and saved correctly with expected data.");
    tagDTO.setName("Test");

    // Act
    adminTagService.createNewTag(tagDTO);

    // Assert
    ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
    verify(tagRepo).save(tagCaptor.capture());
    Tag savedTag = tagCaptor.getValue();

    assertEquals(tagDTO.getName(), savedTag.getName());
    assertEquals(tagDTO.getDescription(), savedTag.getDescription());
  }

  @Test
  void updateTag_Success_OnlyDescriptionModified() {
    // Arrange
    Tag existingTag =
        Tag.builder().name("java").description("Old description").usageCount(5).build();
    UUID tagId = UUID.randomUUID();
    existingTag.setId(tagId);

    TagModificationRequestDTO modificationDTO = new TagModificationRequestDTO();
    modificationDTO.setDescription("Updated description for java tag");

    when(tagRepo.findById(tagId)).thenReturn(Optional.of(existingTag));

    // Act
    adminTagService.updateTag(tagId, modificationDTO);

    // Assert
    ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
    verify(tagRepo).save(tagCaptor.capture());
    Tag savedTag = tagCaptor.getValue();

    assertEquals("Updated description for java tag", savedTag.getDescription());
    assertEquals("java", savedTag.getName(), "Name should remain unchanged");
    assertEquals(5, savedTag.getUsageCount(), "UsageCount should remain unchanged");
  }

  @Test
  void updateTag_TagNotFound() {
    // Arrange
    TagModificationRequestDTO modificationDTO = new TagModificationRequestDTO();
    modificationDTO.setDescription("Some description");
    UUID invalidTagId = UUID.randomUUID();

    when(tagRepo.findById(invalidTagId)).thenReturn(Optional.empty());

    // Act & Assert
    TagNotFoundException exception =
        assertThrows(
            TagNotFoundException.class,
            () -> adminTagService.updateTag(invalidTagId, modificationDTO));

    assertEquals("Tag with id " + invalidTagId + " not found", exception.getMessage());
    verify(tagRepo, never()).save(any());
  }

  @Test
  void deleteTag_Success() {
    // Arrange
    UUID tagId = UUID.randomUUID();
    when(tagRepo.existsById(tagId)).thenReturn(true);

    // Act
    adminTagService.deleteTag(tagId);

    // Assert
    verify(tagRepo).existsById(tagId);
    verify(tagRepo).deleteById(tagId);
  }

  @Test
  void deleteTag_TagNotFound() {
    // Arrange
    UUID invalidTagId = UUID.randomUUID();
    when(tagRepo.existsById(invalidTagId)).thenReturn(false);

    // Act & Assert
    TagNotFoundException exception =
        assertThrows(TagNotFoundException.class, () -> adminTagService.deleteTag(invalidTagId));

    assertEquals("Tag with id " + invalidTagId + " not found", exception.getMessage());
    verify(tagRepo, never()).deleteById(any());
  }
}
