package com.project.hearmeout_backend.user_service.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.authentication_service.model.enums.RoleType;
import com.project.hearmeout_backend.authentication_service.service.implementation.SecurityServiceImpl;
import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.user_service.dto.response.UserDetailResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserProfileResponseDTO;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
public class UserControllerSecurityTest {

  @Autowired private MockMvc mockMvc;

  @Mock private UserServiceImpl userServiceImpl;

  @Mock private SecurityServiceImpl securityServiceImpl;

  private CustomUserDetails verifiedUser;
  private CustomUserDetails standardUser;
  private CustomUserDetails adminUser;

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

    UserDetailResponseDTO adminDto =
        new UserDetailResponseDTO(
            UUID.randomUUID(), "admin_dude", "admin_dude@test.com", "password", RoleType.ADMIN);
    adminUser = new CustomUserDetails(adminDto);
  }

  // ===================== GET /profile/{username} =====================

  @Test
  void userProfile_VerifiedUser_Allowed() throws Exception {
    when(userServiceImpl.getUserProfile(anyString(), eq(verifiedUser.getUserId())))
        .thenReturn(new UserProfileResponseDTO());

    mockMvc.perform(get("/profile/testUser").with(user(verifiedUser))).andExpect(status().isOk());
  }

  @Test
  void userProfile_StandardUser_Allowed() throws Exception {
    when(userServiceImpl.getUserProfile(anyString(), eq(standardUser.getUserId())))
        .thenReturn(new UserProfileResponseDTO());

    mockMvc.perform(get("/profile/testUser").with(user(standardUser))).andExpect(status().isOk());
  }

  @Test
  void userProfile_AdminUser_Allowed() throws Exception {
    when(userServiceImpl.getUserProfile(anyString(), eq(adminUser.getUserId())))
        .thenReturn(new UserProfileResponseDTO());

    mockMvc.perform(get("/profile/testUser").with(user(adminUser))).andExpect(status().isOk());
  }

  @Test
  @WithAnonymousUser
  void userProfile_Anonymous_Unauthorized() throws Exception {
    mockMvc.perform(get("/profile/testUser")).andExpect(status().isUnauthorized());
  }

  // ===================== GET /profile/{username}/error-reports =====================

  @Test
  void userErrorReports_VerifiedUser_Allowed() throws Exception {
    when(userServiceImpl.getUserErrorReports(anyString(), anyInt(), anyInt()))
        .thenReturn(new PagedResponse<>());

    mockMvc
        .perform(get("/profile/testUser/error-reports").with(user(verifiedUser)))
        .andExpect(status().isOk());
  }

  @Test
  void userErrorReports_AdminUser_Allowed() throws Exception {
    when(userServiceImpl.getUserErrorReports(anyString(), anyInt(), anyInt()))
        .thenReturn(new PagedResponse<>());

    mockMvc
        .perform(get("/profile/testUser/error-reports").with(user(adminUser)))
        .andExpect(status().isOk());
  }

  @Test
  void userErrorReports_StandardUser_Forbidden() throws Exception {
    mockMvc
        .perform(get("/profile/testUser/error-reports").with(user(standardUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithAnonymousUser
  void userErrorReports_Anonymous_Unauthorized() throws Exception {
    mockMvc.perform(get("/profile/testUser/error-reports")).andExpect(status().isUnauthorized());
  }

  // ===================== GET /profile/{username}/solutions =====================

  @Test
  void userSolutions_VerifiedUser_Allowed() throws Exception {
    when(userServiceImpl.getUserSolutions(anyString(), anyInt(), anyInt()))
        .thenReturn(new PagedResponse<>());

    mockMvc
        .perform(get("/profile/testUser/solutions").with(user(verifiedUser)))
        .andExpect(status().isOk());
  }

  @Test
  void userSolutions_StandardUser_Forbidden() throws Exception {
    mockMvc
        .perform(get("/profile/testUser/solutions").with(user(standardUser)))
        .andExpect(status().isForbidden());
  }

  // ===================== GET /profile/{username}/comments =====================

  @Test
  void userComments_VerifiedUser_Allowed() throws Exception {
    when(userServiceImpl.getUserComments(anyString(), anyInt(), anyInt()))
        .thenReturn(new PagedResponse<>());

    mockMvc
        .perform(get("/profile/testUser/comments").with(user(verifiedUser)))
        .andExpect(status().isOk());
  }

  @Test
  void userComments_StandardUser_Forbidden() throws Exception {
    mockMvc
        .perform(get("/profile/testUser/comments").with(user(standardUser)))
        .andExpect(status().isForbidden());
  }
}
