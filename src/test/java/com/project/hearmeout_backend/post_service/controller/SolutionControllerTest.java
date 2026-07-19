package com.project.hearmeout_backend.post_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.authentication_service.model.enums.RoleType;
import com.project.hearmeout_backend.authentication_service.service.implementation.SecurityServiceImpl;
import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.post_service.dto.request.AcceptSolutionRequestDTO;
import com.project.hearmeout_backend.post_service.dto.request.SolutionSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.dto.response.SolutionResponseDTO;
import com.project.hearmeout_backend.post_service.service.implementation.SolutionServiceImpl;
import com.project.hearmeout_backend.user_service.dto.response.UserDetailResponseDTO;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class SolutionControllerTest {

  @Mock private SolutionServiceImpl solutionServiceImpl;
  @Mock private SecurityServiceImpl securityServiceImpl;

  @InjectMocks private SolutionController solutionController;

  private CustomUserDetails verifiedUser;
  private CustomUserDetails standardUser;
  private UUID errorReportId;
  private UUID solutionId;

  @BeforeEach
  void setUp() {
    UserDetailResponseDTO verifiedDto =
        new UserDetailResponseDTO(
            UUID.randomUUID(),
            "verified_dude",
            "verified_dude@test.com",
            "password",
            RoleType.VERIFIED_USER);
    verifiedUser = new CustomUserDetails(verifiedDto);

    UserDetailResponseDTO standardDto =
        new UserDetailResponseDTO(
            UUID.randomUUID(),
            "standard_dude",
            "standard_dude@test.com",
            "password",
            RoleType.USER);
    standardUser = new CustomUserDetails(standardDto);

    errorReportId = UUID.randomUUID();
    solutionId = UUID.randomUUID();
  }

  @Test
  void testSubmitSolution_VerifiedUser_Success() throws Exception {
    SolutionSubmitRequestDTO dto = new SolutionSubmitRequestDTO();
    dto.setExplanation("Try restarting your machine.");

    ResponseEntity<String> response =
        solutionController.submitSolution(errorReportId, dto, verifiedUser);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals("Solution created successfully", response.getBody());
    verify(solutionServiceImpl).submitSolution(errorReportId, dto, verifiedUser.getUserId());
  }

  @Test
  void testGetSolutions_WithAuthenticatedUser_Success() throws Exception {
    PagedResponse<SolutionResponseDTO> pagedResponse = new PagedResponse<>();
    when(solutionServiceImpl.getSolutions(
            eq(errorReportId),
            eq(verifiedUser.getUserId()),
            eq(verifiedUser.getUsername()),
            anyInt(),
            anyInt()))
        .thenReturn(pagedResponse);

    ResponseEntity<PagedResponse<SolutionResponseDTO>> response =
        solutionController.getSolutions(errorReportId, 0, 5, verifiedUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(pagedResponse, response.getBody());
  }

  @Test
  void testGetSolutions_AnonymousUser_Success() throws Exception {
    PagedResponse<SolutionResponseDTO> pagedResponse = new PagedResponse<>();
    UUID emptyId = new UUID(0, 0);
    when(solutionServiceImpl.getSolutions(
            eq(errorReportId), eq(emptyId), eq(""), anyInt(), anyInt()))
        .thenReturn(pagedResponse);

    ResponseEntity<PagedResponse<SolutionResponseDTO>> response =
        solutionController.getSolutions(errorReportId, 0, 5, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(pagedResponse, response.getBody());
  }

  @Test
  void testToggleSolutionStatus_Success() throws Exception {
    AcceptSolutionRequestDTO dto = new AcceptSolutionRequestDTO();
    dto.setErrorReportId(errorReportId);
    dto.setSolutionId(solutionId);

    ResponseEntity<String> response = solutionController.toggleSolutionStatus(dto, verifiedUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Solution status updated successfully", response.getBody());
    verify(solutionServiceImpl).handleSolutionStatus(dto, verifiedUser.getUserId());
  }

  @Test
  void testUpdateSolution_Success() throws Exception {
    SolutionSubmitRequestDTO dto = new SolutionSubmitRequestDTO();
    dto.setExplanation("Updated content");

    ResponseEntity<String> response =
        solutionController.updateSolution(solutionId, dto, verifiedUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Solution modified successfully", response.getBody());
    verify(solutionServiceImpl).updateSolution(solutionId, dto, verifiedUser.getUserId());
  }

  @Test
  void testDeleteSolution_Success() throws Exception {
    ResponseEntity<String> response = solutionController.deleteSolution(solutionId, verifiedUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Solution deleted successfully", response.getBody());
    verify(solutionServiceImpl).deleteSolution(solutionId, verifiedUser.getUserId());
  }
}
