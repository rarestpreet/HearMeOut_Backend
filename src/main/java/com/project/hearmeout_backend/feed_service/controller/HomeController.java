package com.project.hearmeout_backend.feed_service.controller;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.feed_service.dto.response.FeedQuestionResponseDTO;
import com.project.hearmeout_backend.feed_service.dto.response.HomeUserProfileResponseDTO;
import com.project.hearmeout_backend.feed_service.service.implementation.HomeServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(
    name = "Home Feed APIs",
    description =
        "Endpoints for fetching the main question feed and user summary for the home page")
public class HomeController {
  private final HomeServiceImpl homeServiceImpl;

  // add search feature without filters

  @Operation(
      summary = "Get question feed",
      description =
          "Retrieves a paginated list of recent questions. Incorporates user-specific data like voting status if the user is authenticated.")
  @GetMapping("")
  public ResponseEntity<@NonNull List<FeedQuestionResponseDTO>> getQuestions(
      @RequestParam(defaultValue = "0") int pageNum,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            homeServiceImpl.generateFeed(
                pageNum, userDetails == null ? null : userDetails.getUserId()));
  }

  @Operation(
      summary = "Get user summary",
      description =
          "Fetches a simplified profile summary for the currently authenticated user to display on the home page.")
  @GetMapping("profile")
  public ResponseEntity<@NonNull HomeUserProfileResponseDTO> getProfile(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(homeServiceImpl.getUserProfile(userDetails == null ? null : userDetails.getUserId()));
  }
}
