package com.project.hearmeout_backend.post_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.authentication_service.model.enums.RoleType;
import com.project.hearmeout_backend.authentication_service.service.implementation.SecurityServiceImpl;
import com.project.hearmeout_backend.post_service.dto.request.ErrorReportSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.dto.response.ErrorReportResponseDTO;
import com.project.hearmeout_backend.post_service.service.implementation.ErrorReportServiceImpl;
import com.project.hearmeout_backend.user_service.dto.response.UserDetailResponseDTO;
import java.util.List;
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
public class ErrorReportControllerTest {

  @Mock private ErrorReportServiceImpl errorReportServiceImpl;
  @Mock private SecurityServiceImpl securityServiceImpl;

  @InjectMocks private ErrorReportController errorReportController;

  private CustomUserDetails verifiedUser;
  private CustomUserDetails standardUser;
  private UUID reportId;

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

    reportId = UUID.randomUUID();
  }

  @Test
  void testSubmitErrorReport_Success() throws Exception {
    ErrorReportSubmitRequestDTO dto = new ErrorReportSubmitRequestDTO();
    dto.setTitle("Sample Error");
    dto.setDescription("I am facing an error");
    dto.setTagIds(List.of(UUID.randomUUID()));

    ResponseEntity<String> response = errorReportController.submitErrorReport(dto, verifiedUser);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals("Error report created successfully", response.getBody());
    verify(errorReportServiceImpl).submitErrorReport(dto, verifiedUser.getUserId());
  }

  @Test
  void testGetErrorReport_WithAuthenticatedUser_Success() throws Exception {
    ErrorReportResponseDTO detailsDto = new ErrorReportResponseDTO();
    // controller calls userDetails.getName() (username), NOT getUsername() (email)
    when(errorReportServiceImpl.getErrorReportDetails(
            eq(reportId),
            eq(verifiedUser.getUserId()),
            eq(verifiedUser.getName()),
            anyInt(),
            anyInt()))
        .thenReturn(detailsDto);

    ResponseEntity<ErrorReportResponseDTO> response =
        errorReportController.getErrorReport(reportId, 0, 5, verifiedUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(detailsDto, response.getBody());
  }

  @Test
  void testGetErrorReport_AnonymousUser_Success() throws Exception {
    ErrorReportResponseDTO detailsDto = new ErrorReportResponseDTO();
    UUID emptyId = new UUID(0, 0);
    when(errorReportServiceImpl.getErrorReportDetails(
            eq(reportId), eq(emptyId), eq(""), anyInt(), anyInt()))
        .thenReturn(detailsDto);

    ResponseEntity<ErrorReportResponseDTO> response =
        errorReportController.getErrorReport(reportId, 0, 5, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(detailsDto, response.getBody());
  }

  @Test
  void testUpdateErrorReport_Success() throws Exception {
    ErrorReportSubmitRequestDTO dto = new ErrorReportSubmitRequestDTO();
    dto.setTitle("Updated Error");
    dto.setDescription("Updated desc");
    dto.setTagIds(List.of(UUID.randomUUID()));

    ResponseEntity<String> response =
        errorReportController.updateErrorReport(reportId, dto, verifiedUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Error report modified successfully", response.getBody());
    verify(errorReportServiceImpl).updateErrorReport(reportId, dto, verifiedUser.getUserId());
  }

  @Test
  void testDeleteErrorReport_Success() throws Exception {
    ResponseEntity<String> response =
        errorReportController.deleteErrorReport(reportId, verifiedUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Error report deleted successfully", response.getBody());
    verify(errorReportServiceImpl).deleteErrorReport(reportId, verifiedUser.getUserId());
  }
}
