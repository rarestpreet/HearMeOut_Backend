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
public class TagModificationRequestDTO {

  @NotNull(message = "info about tag is required")
  @Size(max = 100, message = "only brief info about tag (<100 char)")
  @Schema(description = "A brief description explaining when to use this tag")
  String description;
}
