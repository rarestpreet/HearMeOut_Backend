package com.project.hearmeout_backend.administration_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TagCreationRequestDTO {

  @NotNull(message = "tag name is required")
  @Size(max = 30, message = "name should be short (<15 char)")
  @Schema(description = "The concise name of the tag (e.g., 'java', 'spring-boot')")
  String name;

  @NotNull(message = "info about tag is required")
  @Size(max = 200, message = "only brief info about tag (<100 char)")
  @Schema(description = "A brief description explaining when to use this tag")
  String description;
}
