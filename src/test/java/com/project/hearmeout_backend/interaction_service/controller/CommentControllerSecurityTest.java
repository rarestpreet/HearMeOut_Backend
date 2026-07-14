package com.project.hearmeout_backend.interaction_service.controller;

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
import com.project.hearmeout_backend.interaction_service.dto.request.CommentRequestDTO;
import com.project.hearmeout_backend.interaction_service.service.implementation.CommentServiceImpl;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
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

@WebMvcTest(CommentController.class)
public class CommentControllerSecurityTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Mock private CommentServiceImpl commentServiceImpl;
  @Mock private SecurityServiceImpl securityServiceImpl;

  private CustomUserDetails verifiedUser;
  private CustomUserDetails standardUser;
  private CustomUserDetails adminUser;
  private UUID commentId;
  private UUID parentId;

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

    commentId = UUID.randomUUID();
    parentId = UUID.randomUUID();
  }

  // ===================== POST /comment =====================

  @Test
  void postComment_VerifiedUser_Allowed() throws Exception {
    CommentRequestDTO dto = new CommentRequestDTO();
    dto.setParentId(parentId);
    dto.setParentType(PostType.ERROR_REPORT);
    dto.setBody("I agree");

    mockMvc
        .perform(
            post("/comment")
                .with(user(verifiedUser))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated());
  }

  @Test
  void postComment_StandardUser_Allowed() throws Exception {
    CommentRequestDTO dto = new CommentRequestDTO();
    dto.setParentId(parentId);
    dto.setParentType(PostType.ERROR_REPORT);
    dto.setBody("I agree");

    mockMvc
        .perform(
            post("/comment")
                .with(user(standardUser))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated());
  }

  @Test
  void postComment_AdminUser_Forbidden() throws Exception {
    CommentRequestDTO dto = new CommentRequestDTO();
    dto.setParentId(parentId);
    dto.setParentType(PostType.ERROR_REPORT);
    dto.setBody("I agree");

    mockMvc
        .perform(
            post("/comment")
                .with(user(adminUser))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isForbidden());
  }

  // ===================== GET /comment =====================

  @Test
  void getComments_WithAuthenticatedUser_PassesUsername() throws Exception {
    when(commentServiceImpl.getComments(
            eq(parentId),
            eq(PostType.ERROR_REPORT),
            anyInt(),
            anyInt(),
            eq(verifiedUser.getUsername())))
        .thenReturn(new PagedResponse<>());

    mockMvc
        .perform(
            get("/comment")
                .param("parentId", parentId.toString())
                .param("parentType", "ERROR_REPORT")
                .with(user(verifiedUser)))
        .andExpect(status().isOk());

    verify(commentServiceImpl)
        .getComments(
            eq(parentId),
            eq(PostType.ERROR_REPORT),
            anyInt(),
            anyInt(),
            eq(verifiedUser.getUsername()));
  }

  @Test
  @WithAnonymousUser
  void getComments_AnonymousUser_PassesNullUsername() throws Exception {
    when(commentServiceImpl.getComments(
            eq(parentId), eq(PostType.ERROR_REPORT), anyInt(), anyInt(), eq(null)))
        .thenReturn(new PagedResponse<>());

    mockMvc
        .perform(
            get("/comment")
                .param("parentId", parentId.toString())
                .param("parentType", "ERROR_REPORT"))
        .andExpect(status().isOk());

    verify(commentServiceImpl)
        .getComments(eq(parentId), eq(PostType.ERROR_REPORT), anyInt(), anyInt(), eq(null));
  }

  // ===================== PUT /comment/{id} =====================

  @Test
  void updateComment_VerifiedUser_Allowed() throws Exception {
    CommentRequestDTO dto = new CommentRequestDTO();
    dto.setBody("Updated");

    mockMvc
        .perform(
            put("/comment/" + commentId)
                .with(user(verifiedUser))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk());
  }

  // ===================== DELETE /comment/{id} =====================

  @Test
  void deleteComment_VerifiedUser_Allowed() throws Exception {
    mockMvc
        .perform(delete("/comment/" + commentId).with(user(verifiedUser)).with(csrf()))
        .andExpect(status().isOk());
  }
}
