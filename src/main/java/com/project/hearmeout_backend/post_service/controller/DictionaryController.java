package com.project.hearmeout_backend.post_service.controller;

import com.project.hearmeout_backend.post_service.service.DictionaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("dictionary")
@RequiredArgsConstructor
@Tag(
    name = "Dictionary API",
    description = "Endpoints for fetching dictionary values like profession, language, etc.")
public class DictionaryController {

  private final DictionaryService dictionaryService;

  @GetMapping("{type}")
  @Operation(
      summary = "Get top dictionary values",
      description =
          "Fetch popular/approved values for a specific dictionary type (profession, os, framework, language).")
  public ResponseEntity<List<String>> getTopValues(
      @PathVariable String type,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "10") int limit) {

    List<String> values = dictionaryService.getTopValues(type, search, limit);

    return ResponseEntity.ok(values);
  }
}
