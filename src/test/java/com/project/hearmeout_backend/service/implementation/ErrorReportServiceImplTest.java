package com.project.hearmeout_backend.service.implementation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.common_lib.exception.TagNotFoundException;
import com.project.hearmeout_backend.interaction_service.repository.VoteRepository;
import com.project.hearmeout_backend.post_service.dto.request.ErrorReportSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.model.ErrorReport;
import com.project.hearmeout_backend.post_service.model.Tag;
import com.project.hearmeout_backend.post_service.model.enums.ErrorReportStatus;
import com.project.hearmeout_backend.post_service.repository.ErrorReportRepository;
import com.project.hearmeout_backend.post_service.repository.TagRepository;
import com.project.hearmeout_backend.post_service.service.implementation.ErrorReportServiceImpl;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ErrorReportServiceImplTest {

  @Mock private ErrorReportRepository errorReportRepo;
  @Mock private TagRepository tagRepo;
  @Mock private UserServiceImpl userServiceImpl;
  @Mock private VoteRepository voteRepo;
  @Mock private UserRepository userRepo;

  @InjectMocks private ErrorReportServiceImpl errorReportService;

  private User author;
  private Tag tag1;
  private Tag tag2;
  private Tag tag3;
  private UUID authorId;
  private UUID tag1Id;
  private UUID tag2Id;
  private UUID tag3Id;
  private UUID reportId;

  @BeforeEach
  void setUp() {
    authorId = UUID.randomUUID();
    author = User.builder().username("testUser").reputation(10).build();
    author.setId(authorId);

    tag1Id = UUID.randomUUID();
    tag1 = Tag.builder().name("java").description("Java lang").usageCount(5).build();
    tag1.setId(tag1Id);

    tag2Id = UUID.randomUUID();
    tag2 = Tag.builder().name("spring").description("Spring framework").usageCount(3).build();
    tag2.setId(tag2Id);

    tag3Id = UUID.randomUUID();
    tag3 = Tag.builder().name("kotlin").description("Kotlin lang").usageCount(0).build();
    tag3.setId(tag3Id);

    reportId = UUID.randomUUID();
  }

  // ===================== submitErrorReport tests =====================

  @Test
  void submitErrorReport_IncrementsTagUsageCountAndAuthorReputation() {
    // Arrange
    ErrorReportSubmitRequestDTO dto = new ErrorReportSubmitRequestDTO();
    dto.setTitle("NullPointerException in Spring Boot");
    dto.setDescription("I am getting an NPE when starting the app.");
    dto.setTagIds(List.of(tag1Id, tag2Id));

    when(userServiceImpl.checkAndGetUserByUserId(authorId)).thenReturn(author);
    when(tagRepo.findAllById(List.of(tag1Id, tag2Id))).thenReturn(List.of(tag1, tag2));

    // Act
    errorReportService.submitErrorReport(dto, authorId);

    // Assert
    verify(tagRepo).incrementUsageCount(List.of(tag1Id, tag2Id));
    verify(errorReportRepo).save(any(ErrorReport.class));
    verify(userRepo).save(author);
  }

  @Test
  void submitErrorReport_TagNotFound_ThrowsException() {
    // Arrange
    UUID fakeTagId = UUID.randomUUID();
    ErrorReportSubmitRequestDTO dto = new ErrorReportSubmitRequestDTO();
    dto.setTitle("NullPointerException in Spring Boot");
    dto.setDescription("I am getting an NPE when starting the app.");
    dto.setTagIds(List.of(tag1Id, fakeTagId));

    when(userServiceImpl.checkAndGetUserByUserId(authorId)).thenReturn(author);
    when(tagRepo.findAllById(List.of(tag1Id, fakeTagId)))
        .thenReturn(List.of(tag1)); // Only returns tag1

    // Act & Assert
    assertThrows(
        TagNotFoundException.class, () -> errorReportService.submitErrorReport(dto, authorId));

    verify(tagRepo, never()).incrementUsageCount(any());
    verify(errorReportRepo, never()).save(any(ErrorReport.class));
  }

  // ===================== updateErrorReport tests =====================

  @Test
  void updateErrorReport_SameTags_NoCountChange() {
    // Arrange
    ErrorReport report =
        ErrorReport.builder()
            .status(ErrorReportStatus.OPEN)
            .tags(new ArrayList<>(List.of(tag1, tag2)))
            .build();
    report.setAuthor(author);
    report.setId(reportId);

    ErrorReportSubmitRequestDTO dto = new ErrorReportSubmitRequestDTO();
    dto.setTitle("Updated Title");
    dto.setTagIds(List.of(tag1Id, tag2Id));

    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(report));
    when(tagRepo.findAllById(List.of(tag1Id, tag2Id))).thenReturn(List.of(tag1, tag2));

    // Act
    errorReportService.updateErrorReport(reportId, dto, authorId);

    // Assert
    verify(tagRepo, never()).incrementUsageCount(any());
    verify(tagRepo, never()).decrementUsageCount(any());
    verify(errorReportRepo).save(report);
  }

  @Test
  void updateErrorReport_TagsChanged_IncrementsAndDecrements() {
    // Arrange — report has tag1, tag2. Updating to tag2, tag3.
    ErrorReport report =
        ErrorReport.builder()
            .status(ErrorReportStatus.OPEN)
            .tags(new ArrayList<>(List.of(tag1, tag2)))
            .build();
    report.setAuthor(author);
    report.setId(reportId);

    ErrorReportSubmitRequestDTO dto = new ErrorReportSubmitRequestDTO();
    dto.setTitle("Updated Title");
    dto.setTagIds(List.of(tag2Id, tag3Id));

    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(report));
    when(tagRepo.findAllById(List.of(tag2Id, tag3Id))).thenReturn(List.of(tag2, tag3));

    // Act
    errorReportService.updateErrorReport(reportId, dto, authorId);

    // Assert
    verify(tagRepo).decrementUsageCount(List.of(tag1Id));
    verify(tagRepo).incrementUsageCount(List.of(tag3Id));
    verify(errorReportRepo).save(report);
  }

  @Test
  void updateErrorReport_NotAuthor_ThrowsException() {
    // Arrange
    UUID otherUserId = UUID.randomUUID();
    User otherUser = User.builder().username("other").build();
    otherUser.setId(otherUserId);

    ErrorReport report =
        ErrorReport.builder()
            .status(ErrorReportStatus.OPEN)
            .tags(new ArrayList<>(List.of(tag1)))
            .build();
    report.setAuthor(author);
    report.setId(reportId);

    ErrorReportSubmitRequestDTO dto = new ErrorReportSubmitRequestDTO();
    dto.setTagIds(List.of(tag3Id));

    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(report));

    // Act & Assert
    assertThrows(
        InvalidOperationException.class,
        () -> errorReportService.updateErrorReport(reportId, dto, otherUserId));

    verify(tagRepo, never()).incrementUsageCount(any());
    verify(tagRepo, never()).decrementUsageCount(any());
  }

  @Test
  void updateErrorReport_ResolvedReport_ThrowsException() {
    // Arrange
    ErrorReport report =
        ErrorReport.builder()
            .status(ErrorReportStatus.RESOLVED)
            .tags(new ArrayList<>(List.of(tag1)))
            .build();
    report.setAuthor(author);
    report.setId(reportId);

    ErrorReportSubmitRequestDTO dto = new ErrorReportSubmitRequestDTO();
    dto.setTagIds(List.of(tag3Id));

    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(report));

    // Act & Assert
    assertThrows(
        InvalidOperationException.class,
        () -> errorReportService.updateErrorReport(reportId, dto, authorId));
  }

  // ===================== deleteErrorReport tests =====================

  @Test
  void deleteErrorReport_DecrementsTagUsageCount() {
    // Arrange
    ErrorReport report =
        ErrorReport.builder()
            .status(ErrorReportStatus.OPEN)
            .tags(new ArrayList<>(List.of(tag1, tag2)))
            .build();
    report.setAuthor(author);
    report.setId(reportId);

    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(report));
    when(userServiceImpl.checkAndGetUserByUserId(authorId)).thenReturn(author);
    when(voteRepo.findAllByParentIdAndParentType(eq(reportId), any())).thenReturn(List.of());

    // Act
    errorReportService.deleteErrorReport(reportId, authorId);

    // Assert
    verify(tagRepo).decrementUsageCount(List.of(tag1Id, tag2Id));
    verify(errorReportRepo).delete(report);
  }

  @Test
  void deleteErrorReport_ResolvedReport_ThrowsException() {
    // Arrange
    ErrorReport report =
        ErrorReport.builder()
            .status(ErrorReportStatus.RESOLVED)
            .tags(new ArrayList<>(List.of(tag1)))
            .build();
    report.setAuthor(author);
    report.setId(reportId);

    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(report));

    // Act & Assert
    assertThrows(
        InvalidOperationException.class,
        () -> errorReportService.deleteErrorReport(reportId, authorId));

    verify(tagRepo, never()).decrementUsageCount(any());
    verify(errorReportRepo, never()).delete(any(ErrorReport.class));
  }
}
