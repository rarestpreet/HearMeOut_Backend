package com.project.hearmeout_backend.interaction_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.authentication_service.model.enums.RoleType;
import com.project.hearmeout_backend.authentication_service.service.implementation.SecurityServiceImpl;
import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.interaction_service.dto.request.CommentRequestDTO;
import com.project.hearmeout_backend.interaction_service.dto.response.CommentResponseDTO;
import com.project.hearmeout_backend.interaction_service.service.implementation.CommentServiceImpl;
import com.project.hearmeout_backend.post_service.model.enums.PostType;
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
public class CommentControllerTest {

  @Mock private CommentServiceImpl commentServiceImpl;
  @Mock private SecurityServiceImpl securityServiceImpl;

  @InjectMocks private CommentController commentController;

  private CustomUserDetails verifiedUser;
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

    commentId = UUID.randomUUID();
    parentId = UUID.randomUUID();
  }

  @Test
  void testPostComment_Success() throws Exception {
    CommentRequestDTO dto = new CommentRequestDTO();
    dto.setParentId(parentId);
    dto.setParentType(PostType.ERROR_REPORT);
    dto.setBody("I agree");

    ResponseEntity<String> response = commentController.postComment(dto, verifiedUser);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals("Comment was added successfully", response.getBody());
    verify(commentServiceImpl).createNewComment(dto, verifiedUser.getUserId());
  }

  @Test
  void testGetComments_WithAuthenticatedUser_Success() throws Exception {
    PagedResponse<CommentResponseDTO> pagedResponse = new PagedResponse<>();
    when(commentServiceImpl.getComments(
            eq(parentId),
            eq(PostType.ERROR_REPORT),
            anyInt(),
            anyInt(),
            eq(verifiedUser.getUsername())))
        .thenReturn(pagedResponse);

    ResponseEntity<PagedResponse<CommentResponseDTO>> response =
        commentController.getComments(parentId, PostType.ERROR_REPORT, 5, 0, verifiedUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(pagedResponse, response.getBody());
  }

  @Test
  void testGetComments_AnonymousUser_Success() throws Exception {
    PagedResponse<CommentResponseDTO> pagedResponse = new PagedResponse<>();
    when(commentServiceImpl.getComments(
            eq(parentId), eq(PostType.ERROR_REPORT), anyInt(), anyInt(), eq(null)))
        .thenReturn(pagedResponse);

    ResponseEntity<PagedResponse<CommentResponseDTO>> response =
        commentController.getComments(parentId, PostType.ERROR_REPORT, 5, 0, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(pagedResponse, response.getBody());
  }

  @Test
  void testUpdateComment_Success() throws Exception {
    CommentRequestDTO dto = new CommentRequestDTO();
    dto.setBody("Updated");

    ResponseEntity<String> response = commentController.updateComment(commentId, dto, verifiedUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Comment was updated successfully", response.getBody());
    verify(commentServiceImpl)
        .updateCommentBody(commentId, dto.getBody(), verifiedUser.getUserId());
  }

  @Test
  void testDeleteComment_Success() throws Exception {
    ResponseEntity<String> response = commentController.deleteComment(commentId, verifiedUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Comment was deleted successfully", response.getBody());
    verify(commentServiceImpl).removeComment(commentId, verifiedUser.getUserId());
  }
}
