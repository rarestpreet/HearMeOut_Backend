package com.project.hearmeout_backend.user_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.authentication_service.model.enums.RoleType;
import com.project.hearmeout_backend.authentication_service.service.implementation.SecurityServiceImpl;
import com.project.hearmeout_backend.common_lib.dto.PagedResponse;
import com.project.hearmeout_backend.user_service.dto.request.UserProfileModificationRequestDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserAnswerResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserCommentResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserDetailResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserErrorReportResponseDTO;
import com.project.hearmeout_backend.user_service.dto.response.UserProfileResponseDTO;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

  @Mock private UserServiceImpl userServiceImpl;
  @Mock private SecurityServiceImpl securityServiceImpl;

  @InjectMocks private UserController userController;

  private CustomUserDetails verifiedUser;
  private CustomUserDetails standardUser;
  private CustomUserDetails adminUser;

  private UUID verifiedUserId;
  private UUID standardUserId;
  private UUID adminUserId;

  @BeforeEach
  void setUp() {
    verifiedUserId = UUID.randomUUID();
    UserDetailResponseDTO verifiedDto =
        new UserDetailResponseDTO(
            verifiedUserId,
            "verified_dude",
            "verified_dude@test.com",
            "password",
            RoleType.VERIFIED_USER);
    verifiedUser = new CustomUserDetails(verifiedDto);

    standardUserId = UUID.randomUUID();
    UserDetailResponseDTO standardDto =
        new UserDetailResponseDTO(
            standardUserId, "standard_dude", "standard_dude@test.com", "password", RoleType.USER);
    standardUser = new CustomUserDetails(standardDto);

    adminUserId = UUID.randomUUID();
    UserDetailResponseDTO adminDto =
        new UserDetailResponseDTO(
            adminUserId, "admin_dude", "admin_dude@test.com", "password", RoleType.ADMIN);
    adminUser = new CustomUserDetails(adminDto);
  }

  @Test
  void testUserProfile_VerifiedUser_Success() throws Exception {
    UserProfileResponseDTO profile = new UserProfileResponseDTO();
    when(userServiceImpl.getUserProfile(verifiedUser.getName(), verifiedUserId))
        .thenReturn(profile);

    ResponseEntity<UserProfileResponseDTO> response =
        userController.userProfile(verifiedUser.getName(), verifiedUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(profile, response.getBody());
    verify(userServiceImpl).getUserProfile(verifiedUser.getName(), verifiedUserId);
  }

  @Test
  void testUserProfile_StandardUser_Success() throws Exception {
    UserProfileResponseDTO profile = new UserProfileResponseDTO();
    when(userServiceImpl.getUserProfile(standardUser.getName(), standardUserId))
        .thenReturn(profile);

    ResponseEntity<UserProfileResponseDTO> response =
        userController.userProfile(standardUser.getName(), standardUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(profile, response.getBody());
  }

  @Test
  void testUserProfile_Anonymous_Success() throws Exception {
    UserProfileResponseDTO profile = new UserProfileResponseDTO();
    when(userServiceImpl.getUserProfile(verifiedUser.getName(), null)).thenReturn(profile);

    ResponseEntity<UserProfileResponseDTO> response =
        userController.userProfile(verifiedUser.getName(), null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(profile, response.getBody());
  }

  @Test
  void testUserErrorReports_Success() throws Exception {
    PagedResponse<UserErrorReportResponseDTO> pagedResponse = new PagedResponse<>();
    when(userServiceImpl.getUserErrorReports(verifiedUser.getName(), 5, 0))
        .thenReturn(pagedResponse);

    ResponseEntity<PagedResponse<UserErrorReportResponseDTO>> response =
        userController.userErrorReports(verifiedUser.getName(), 5, 0);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(pagedResponse, response.getBody());
  }

  @Test
  void testUserSolutions_Success() throws Exception {
    PagedResponse<UserAnswerResponseDTO> pagedResponse = new PagedResponse<>();
    when(userServiceImpl.getUserSolutions(verifiedUser.getName(), 5, 0)).thenReturn(pagedResponse);

    ResponseEntity<PagedResponse<UserAnswerResponseDTO>> response =
        userController.userSolutions(verifiedUser.getName(), 5, 0);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(pagedResponse, response.getBody());
  }

  @Test
  void testUserComments_Success() throws Exception {
    PagedResponse<UserCommentResponseDTO> pagedResponse = new PagedResponse<>();
    when(userServiceImpl.getUserComments(verifiedUser.getName(), 5, 0)).thenReturn(pagedResponse);

    ResponseEntity<PagedResponse<UserCommentResponseDTO>> response =
        userController.userComments(verifiedUser.getName(), 5, 0);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(pagedResponse, response.getBody());
  }

  @Test
  void testUpdateUserProfile_EmailChanged_Success() throws Exception {
    UserProfileModificationRequestDTO requestDTO =
        new UserProfileModificationRequestDTO(
            "new_username", "new_email@test.com", "John Doe", "Bio", "Dev");
    when(userServiceImpl.updateUserDetails(requestDTO, verifiedUserId)).thenReturn(true);
    ResponseCookie cookie1 = ResponseCookie.from("cookie1", "val1").build();
    ResponseCookie cookie2 = ResponseCookie.from("cookie2", "val2").build();

    MockHttpServletRequest request = new MockHttpServletRequest();
    Cookie[] cookies = new Cookie[] {new Cookie("refresh-token", "dummy-token")};
    request.setCookies(cookies);

    when(securityServiceImpl.terminateSession(verifiedUser.getUsername(), cookies))
        .thenReturn(List.of(cookie1, cookie2));

    ResponseEntity<String> response =
        userController.updateUserProfile(verifiedUser.getName(), requestDTO, verifiedUser, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Details updated Successfully", response.getBody());
    verify(securityServiceImpl).terminateSession(verifiedUser.getUsername(), request.getCookies());
  }

  @Test
  void testUpdateUserProfile_EmailNotChanged_Success() throws Exception {
    UserProfileModificationRequestDTO requestDTO =
        new UserProfileModificationRequestDTO(
            "new_username", verifiedUser.getUsername(), "John Doe", "Bio", "Dev");
    when(userServiceImpl.updateUserDetails(requestDTO, verifiedUserId)).thenReturn(false);

    MockHttpServletRequest request = new MockHttpServletRequest();

    ResponseEntity<String> response =
        userController.updateUserProfile(verifiedUser.getName(), requestDTO, verifiedUser, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Details updated Successfully", response.getBody());
    verifyNoInteractions(securityServiceImpl);
  }

  @Test
  void testDeleteUser_Success() throws Exception {

    ResponseCookie cookie1 = ResponseCookie.from("jwt-token", "").build();
    ResponseCookie cookie2 = ResponseCookie.from("refresh-token", "").build();
    MockHttpServletRequest request = new MockHttpServletRequest();
    Cookie[] cookies = new Cookie[] {new Cookie("refresh-token", "dummy-token")};
    request.setCookies(cookies);

    when(securityServiceImpl.terminateSession(verifiedUser.getUsername(), cookies))
        .thenReturn(List.of(cookie1, cookie2));

    ResponseEntity<String> response =
        userController.deleteUser(verifiedUser.getName(), verifiedUser, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Account deleted Successfully", response.getBody());

    assertEquals(2, response.getHeaders().get(HttpHeaders.SET_COOKIE).size());

    verify(userServiceImpl).terminateUserAccount(verifiedUserId);
    verify(securityServiceImpl).terminateSession(verifiedUser.getUsername(), request.getCookies());
  }
}
