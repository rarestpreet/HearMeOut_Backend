package com.project.hearmeout_backend.post_service.mapper;

import com.project.hearmeout_backend.post_service.dto.request.SolutionSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.model.ErrorReport;
import com.project.hearmeout_backend.post_service.model.Framework;
import com.project.hearmeout_backend.post_service.model.OperatingSystem;
import com.project.hearmeout_backend.post_service.model.ProgrammingLanguage;
import com.project.hearmeout_backend.post_service.model.Solution;
import com.project.hearmeout_backend.post_service.model.enums.SolutionStatus;
import com.project.hearmeout_backend.user_service.model.User;

public class SolutionMapper {

  public static Solution toEntity(
      SolutionSubmitRequestDTO dto,
      ErrorReport errorReport,
      User author,
      ProgrammingLanguage language,
      Framework framework,
      OperatingSystem os) {
    Solution solution =
        Solution.builder()
            .errorReport(errorReport)
            .probableCause(dto.getProbableCause())
            .explanation(dto.getExplanation())
            .codeChange(dto.getCodeChange())
            .status(SolutionStatus.PENDING)
            .build();

    solution.setAuthor(author);
    solution.setLanguage(language);
    solution.setLanguageVersion(dto.getLanguageVersion());
    solution.setFramework(framework);
    solution.setFrameworkVersion(dto.getFrameworkVersion());
    solution.setOs(os);
    solution.setOsVersion(dto.getOsVersion());

    return solution;
  }
}
