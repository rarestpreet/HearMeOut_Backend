package com.project.hearmeout_backend.post_service.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ErrorReportController.class)
public class ErrorReportControllerSecurityTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Mock private ErrorReportServiceImpl errorReportServiceImpl;
  @Mock private SecurityServiceImpl securityServiceImpl;

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

  // ===================== POST /error-report =====================

  @Test
  void submitErrorReport_VerifiedUser_Allowed() throws Exception {
    ErrorReportSubmitRequestDTO dto = new ErrorReportSubmitRequestDTO();
    dto.setTitle("Sample Error");
    dto.setDescription("I am facing an error");
    dto.setTagIds(List.of(UUID.randomUUID()));

    mockMvc
        .perform(
            post("/error-report")
                .with(user(verifiedUser))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated());
  }

  @Test
  void submitErrorReport_StandardUser_Forbidden() throws Exception {
    ErrorReportSubmitRequestDTO dto = new ErrorReportSubmitRequestDTO();
    dto.setTitle("Sample Error");
    dto.setDescription("I am facing an error");
    dto.setTagIds(List.of(UUID.randomUUID()));

    mockMvc
        .perform(
            post("/error-report")
                .with(user(standardUser))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isForbidden());
  }

  // ===================== GET /error-report/{id} =====================

  @Test
  void getErrorReport_WithAuthenticatedUser_PassesUserIdForOperableCheck() throws Exception {
    when(errorReportServiceImpl.getErrorReportDetails(
            eq(reportId),
            eq(verifiedUser.getUserId()),
            eq(verifiedUser.getUsername()),
            anyInt(),
            anyInt()))
        .thenReturn(new ErrorReportResponseDTO());

    mockMvc
        .perform(get("/error-report/" + reportId).with(user(verifiedUser)))
        .andExpect(status().isOk());

    verify(errorReportServiceImpl)
        .getErrorReportDetails(
            eq(reportId),
            eq(verifiedUser.getUserId()),
            eq(verifiedUser.getUsername()),
            anyInt(),
            anyInt());
  }

  @Test
  @WithAnonymousUser
  void getErrorReport_AnonymousUser_PassesEmptyUUIDForOperableCheck() throws Exception {
    UUID emptyId = new UUID(0, 0);

    when(errorReportServiceImpl.getErrorReportDetails(
            eq(reportId), eq(emptyId), eq(""), anyInt(), anyInt()))
        .thenReturn(new ErrorReportResponseDTO());

    mockMvc.perform(get("/error-report/" + reportId)).andExpect(status().isOk());

    verify(errorReportServiceImpl)
        .getErrorReportDetails(eq(reportId), eq(emptyId), eq(""), anyInt(), anyInt());
  }

  // ===================== PUT /error-report/{id} =====================

  @Test
  void updateErrorReport_VerifiedUser_Allowed() throws Exception {
    ErrorReportSubmitRequestDTO dto = new ErrorReportSubmitRequestDTO();
    dto.setTitle("Updated Error");
    dto.setDescription("Updated desc");
    dto.setTagIds(List.of(UUID.randomUUID()));

    mockMvc
        .perform(
            put("/error-report/" + reportId)
                .with(user(verifiedUser))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk());
  }

  @Test
  void updateErrorReport_StandardUser_Forbidden() throws Exception {
    ErrorReportSubmitRequestDTO dto = new ErrorReportSubmitRequestDTO();
    dto.setTitle("Updated Error");
    dto.setDescription("Updated desc");
    dto.setTagIds(List.of(UUID.randomUUID()));

    mockMvc
        .perform(
            put("/error-report/" + reportId)
                .with(user(standardUser))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isForbidden());
  }

  // ===================== DELETE /error-report/{id} =====================

  @Test
  void deleteErrorReport_VerifiedUser_Allowed() throws Exception {
    mockMvc
        .perform(delete("/error-report/" + reportId).with(user(verifiedUser)).with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  void deleteErrorReport_StandardUser_Forbidden() throws Exception {
    mockMvc
        .perform(delete("/error-report/" + reportId).with(user(standardUser)).with(csrf()))
        .andExpect(status().isForbidden());
  }
}
