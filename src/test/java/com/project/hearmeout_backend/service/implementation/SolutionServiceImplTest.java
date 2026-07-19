package com.project.hearmeout_backend.service.implementation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.post_service.dto.request.AcceptSolutionRequestDTO;
import com.project.hearmeout_backend.post_service.dto.request.SolutionSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.model.ErrorReport;
import com.project.hearmeout_backend.post_service.model.Framework;
import com.project.hearmeout_backend.post_service.model.OperatingSystem;
import com.project.hearmeout_backend.post_service.model.ProgrammingLanguage;
import com.project.hearmeout_backend.post_service.model.Solution;
import com.project.hearmeout_backend.post_service.model.enums.ErrorReportStatus;
import com.project.hearmeout_backend.post_service.model.enums.SolutionStatus;
import com.project.hearmeout_backend.post_service.repository.ErrorReportRepository;
import com.project.hearmeout_backend.post_service.repository.FrameworkRepository;
import com.project.hearmeout_backend.post_service.repository.OperatingSystemRepository;
import com.project.hearmeout_backend.post_service.repository.ProgrammingLanguageRepository;
import com.project.hearmeout_backend.post_service.repository.SolutionRepository;
import com.project.hearmeout_backend.post_service.service.implementation.SolutionServiceImpl;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SolutionServiceImplTest {

  @Mock private SolutionRepository solutionRepo;
  @Mock private ErrorReportRepository errorReportRepo;
  @Mock private UserServiceImpl userServiceImpl;
  @Mock private UserRepository userRepo;
  @Mock private ProgrammingLanguageRepository languageRepo;
  @Mock private FrameworkRepository frameworkRepo;
  @Mock private OperatingSystemRepository osRepo;

  @InjectMocks private SolutionServiceImpl solutionService;

  private User reportAuthor;
  private User solutionAuthor;
  private ErrorReport errorReport;
  private Solution solution;
  private UUID reportAuthorId;
  private UUID solutionAuthorId;
  private UUID reportId;
  private UUID solutionId;

  @BeforeEach
  void setUp() {
    reportAuthorId = UUID.randomUUID();
    reportAuthor = User.builder().username("reportUser").reputation(10).build();
    reportAuthor.setId(reportAuthorId);

    solutionAuthorId = UUID.randomUUID();
    solutionAuthor = User.builder().username("solutionUser").reputation(20).build();
    solutionAuthor.setId(solutionAuthorId);

    reportId = UUID.randomUUID();
    errorReport =
        ErrorReport.builder().status(ErrorReportStatus.OPEN).solutions(new ArrayList<>()).build();
    errorReport.setId(reportId);
    errorReport.setAuthor(reportAuthor);

    solutionId = UUID.randomUUID();
    solution = Solution.builder().errorReport(errorReport).status(SolutionStatus.PENDING).build();
    solution.setId(solutionId);
    solution.setAuthor(solutionAuthor);
  }

  // ===================== submitSolution tests =====================

  @Test
  void submitSolution_Valid_SavesSolutionAndUpdatesReputation() {
    SolutionSubmitRequestDTO dto = new SolutionSubmitRequestDTO();
    dto.setExplanation("Here is how to fix it.");
    dto.setLanguage("Java");
    dto.setFramework("Spring");
    dto.setOs("Linux");

    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(errorReport));
    when(userServiceImpl.checkAndGetUserByUserId(solutionAuthorId)).thenReturn(solutionAuthor);

    when(languageRepo.findByNameIgnoreCase("Java"))
        .thenReturn(Optional.of(ProgrammingLanguage.builder().name("JAVA").build()));
    when(frameworkRepo.findByNameIgnoreCase("Spring"))
        .thenReturn(Optional.of(Framework.builder().name("SPRING").build()));
    when(osRepo.findByNameIgnoreCase("Linux"))
        .thenReturn(Optional.of(OperatingSystem.builder().name("LINUX").build()));

    solutionService.submitSolution(reportId, dto, solutionAuthorId);

    verify(userRepo).save(solutionAuthor);
    verify(solutionRepo).save(any(Solution.class));
  }

  @Test
  void submitSolution_SameAuthor_ThrowsException() {
    SolutionSubmitRequestDTO dto = new SolutionSubmitRequestDTO();
    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(errorReport));

    assertThrows(
        InvalidOperationException.class,
        () -> solutionService.submitSolution(reportId, dto, reportAuthorId));
  }

  @Test
  void submitSolution_ResolvedReport_ThrowsException() {
    errorReport.setStatus(ErrorReportStatus.RESOLVED);
    SolutionSubmitRequestDTO dto = new SolutionSubmitRequestDTO();

    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(errorReport));

    assertThrows(
        InvalidOperationException.class,
        () -> solutionService.submitSolution(reportId, dto, solutionAuthorId));
  }

  // ===================== handleSolutionStatus tests =====================

  @Test
  void handleSolutionStatus_AcceptSolution_UpdatesStatusAndReputation() {
    AcceptSolutionRequestDTO dto = new AcceptSolutionRequestDTO(reportId, solutionId);

    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(errorReport));
    when(solutionRepo.findById(solutionId)).thenReturn(Optional.of(solution));
    when(userServiceImpl.checkAndGetUserByUserId(solutionAuthorId)).thenReturn(solutionAuthor);
    when(userServiceImpl.checkAndGetUserByUserId(reportAuthorId)).thenReturn(reportAuthor);

    solutionService.handleSolutionStatus(dto, reportAuthorId);

    verify(errorReportRepo).save(errorReport);
    verify(solutionRepo).save(solution);
    verify(userRepo).save(reportAuthor);
    verify(userRepo).save(solutionAuthor);
  }

  @Test
  void handleSolutionStatus_NotReportAuthor_ThrowsException() {
    AcceptSolutionRequestDTO dto = new AcceptSolutionRequestDTO(reportId, solutionId);
    UUID otherUserId = UUID.randomUUID();

    when(errorReportRepo.findById(reportId)).thenReturn(Optional.of(errorReport));
    when(solutionRepo.findById(solutionId)).thenReturn(Optional.of(solution));
    when(userServiceImpl.checkAndGetUserByUserId(solutionAuthorId)).thenReturn(solutionAuthor);
    when(userServiceImpl.checkAndGetUserByUserId(reportAuthorId)).thenReturn(reportAuthor);

    assertThrows(
        InvalidOperationException.class,
        () -> solutionService.handleSolutionStatus(dto, otherUserId));
  }

  // ===================== updateSolution tests =====================

  @Test
  void updateSolution_Valid_UpdatesFields() {
    SolutionSubmitRequestDTO dto = new SolutionSubmitRequestDTO();
    dto.setExplanation("Updated explanation");
    dto.setLanguage("Java");
    dto.setFramework("Spring");
    dto.setOs("Linux");

    when(solutionRepo.findById(solutionId)).thenReturn(Optional.of(solution));
    when(languageRepo.findByNameIgnoreCase("Java"))
        .thenReturn(Optional.of(ProgrammingLanguage.builder().name("JAVA").build()));
    when(frameworkRepo.findByNameIgnoreCase("Spring"))
        .thenReturn(Optional.of(Framework.builder().name("SPRING").build()));
    when(osRepo.findByNameIgnoreCase("Linux"))
        .thenReturn(Optional.of(OperatingSystem.builder().name("LINUX").build()));

    solutionService.updateSolution(solutionId, dto, solutionAuthorId);

    verify(solutionRepo).save(solution);
  }

  @Test
  void updateSolution_NotAuthor_ThrowsException() {
    SolutionSubmitRequestDTO dto = new SolutionSubmitRequestDTO();
    UUID otherUserId = UUID.randomUUID();

    when(solutionRepo.findById(solutionId)).thenReturn(Optional.of(solution));

    assertThrows(
        InvalidOperationException.class,
        () -> solutionService.updateSolution(solutionId, dto, otherUserId));
  }

  @Test
  void updateSolution_AcceptedSolution_ThrowsException() {
    solution.setStatus(SolutionStatus.ACCEPTED);
    SolutionSubmitRequestDTO dto = new SolutionSubmitRequestDTO();

    when(solutionRepo.findById(solutionId)).thenReturn(Optional.of(solution));

    assertThrows(
        InvalidOperationException.class,
        () -> solutionService.updateSolution(solutionId, dto, solutionAuthorId));
  }
}
