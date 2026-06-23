package com.project.hearmeout_backend.post_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestionSubmitRequestDTO {

  @NotBlank(message = "Question title is required")
  @Size(min = 50, max = 150, message = "Question must be 15 to 150 characters long")
  @Schema(description = "A concise summary of the question being asked")
  private String title;

  @NotBlank(message = "Description is required")
  @Size(min = 200, max = 2000, message = "Describe question in 50 to 500 characters")
  @Schema(description = "The detailed content of the question, providing context and specifics")
  private String body;

  @NotEmpty(message = "At least one tag is required")
  @Size(min = 1, max = 5, message = "Must contain 1 to 10 tags")
  @Schema(description = "A list of tag IDs categorizing the question")
  private List<Long> tagIds;
}
