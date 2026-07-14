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
import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.post_service.dto.request.AcceptSolutionRequestDTO;
import com.project.hearmeout_backend.post_service.dto.request.SolutionSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.service.implementation.SolutionServiceImpl;
import com.project.hearmeout_backend.user_service.dto.response.UserDetailResponseDTO;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SolutionController.class)
public class SolutionControllerSecurityTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Mock private SolutionServiceImpl solutionServiceImpl;
  @Mock private SecurityServiceImpl securityServiceImpl;

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

  // ===================== POST /solution/error-report/{id} =====================

  @Test
  void submitSolution_VerifiedUser_Allowed() throws Exception {
    SolutionSubmitRequestDTO dto = new SolutionSubmitRequestDTO();
    dto.setExplanation("Try restarting your machine.");

    mockMvc
        .perform(
            post("/solution/error-report/" + errorReportId)
                .with(user(verifiedUser))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated());
  }

  @Test
  void submitSolution_StandardUser_Forbidden() throws Exception {
    SolutionSubmitRequestDTO dto = new SolutionSubmitRequestDTO();
    dto.setExplanation("Try restarting your machine.");

    mockMvc
        .perform(
            post("/solution/error-report/" + errorReportId)
                .with(user(standardUser))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isForbidden());
  }

  // ===================== GET /solution/error-report/{id} =====================

  @Test
  void getSolutions_WithAuthenticatedUser_PassesUserIdForOperableCheck() throws Exception {
    when(solutionServiceImpl.getSolutions(
            eq(errorReportId),
            eq(verifiedUser.getUserId()),
            eq(verifiedUser.getUsername()),
            anyInt(),
            anyInt()))
        .thenReturn(new PagedResponse<>());

    mockMvc
        .perform(get("/solution/error-report/" + errorReportId).with(user(verifiedUser)))
        .andExpect(status().isOk());

    verify(solutionServiceImpl)
        .getSolutions(
            eq(errorReportId),
            eq(verifiedUser.getUserId()),
            eq(verifiedUser.getUsername()),
            anyInt(),
            anyInt());
  }

  @Test
  @WithAnonymousUser
  void getSolutions_AnonymousUser_PassesEmptyUUIDForOperableCheck() throws Exception {
    UUID emptyId = new UUID(0, 0);

    when(solutionServiceImpl.getSolutions(
            eq(errorReportId), eq(emptyId), eq(""), anyInt(), anyInt()))
        .thenReturn(new PagedResponse<>());

    mockMvc.perform(get("/solution/error-report/" + errorReportId)).andExpect(status().isOk());

    verify(solutionServiceImpl)
        .getSolutions(eq(errorReportId), eq(emptyId), eq(""), anyInt(), anyInt());
  }

  // ===================== POST /solution/toggleStatus =====================

  @Test
  void toggleSolutionStatus_VerifiedUser_Allowed() throws Exception {
    AcceptSolutionRequestDTO dto = new AcceptSolutionRequestDTO();
    dto.setErrorReportId(errorReportId);
    dto.setSolutionId(solutionId);

    mockMvc
        .perform(
            post("/solution/toggleStatus")
                .with(user(verifiedUser))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk());
  }

  @Test
  void toggleSolutionStatus_StandardUser_Forbidden() throws Exception {
    AcceptSolutionRequestDTO dto = new AcceptSolutionRequestDTO();
    dto.setErrorReportId(errorReportId);
    dto.setSolutionId(solutionId);

    mockMvc
        .perform(
            post("/solution/toggleStatus")
                .with(user(standardUser))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isForbidden());
  }

  // ===================== PUT /solution/{id} =====================

  @Test
  void updateSolution_VerifiedUser_Allowed() throws Exception {
    SolutionSubmitRequestDTO dto = new SolutionSubmitRequestDTO();
    dto.setExplanation("Updated content");

    mockMvc
        .perform(
            put("/solution/" + solutionId)
                .with(user(verifiedUser))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk());
  }

  @Test
  void updateSolution_StandardUser_Forbidden() throws Exception {
    SolutionSubmitRequestDTO dto = new SolutionSubmitRequestDTO();

    dto.setExplanation("Updated content");

    mockMvc
        .perform(
            put("/solution/" + solutionId)
                .with(user(standardUser))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isForbidden());
  }

  // ===================== DELETE /solution/{id} =====================

  @Test
  void deleteSolution_VerifiedUser_Allowed() throws Exception {
    mockMvc
        .perform(delete("/solution/" + solutionId).with(user(verifiedUser)).with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  void deleteSolution_StandardUser_Forbidden() throws Exception {
    mockMvc
        .perform(delete("/solution/" + solutionId).with(user(standardUser)).with(csrf()))
        .andExpect(status().isForbidden());
  }
}
