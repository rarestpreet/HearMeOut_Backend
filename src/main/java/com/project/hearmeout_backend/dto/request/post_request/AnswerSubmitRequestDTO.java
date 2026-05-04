package com.project.hearmeout_backend.dto.request.post_request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnswerSubmitRequestDTO {

    @NotBlank(message = "Answer is required")
    @Schema(description = "The main content of the answer, explaining the solution or providing information")
    @Size(min = 150, max = 1500, message = "Describe question in 200 to 1500 characters")
    private String body;
}