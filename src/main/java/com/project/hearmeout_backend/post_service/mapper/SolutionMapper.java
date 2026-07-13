package com.project.hearmeout_backend.post_service.mapper;

import com.project.hearmeout_backend.post_service.dto.request.SolutionSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.model.ErrorReport;
import com.project.hearmeout_backend.post_service.model.Solution;
import com.project.hearmeout_backend.post_service.model.enums.SolutionStatus;
import com.project.hearmeout_backend.user_service.model.User;

public class SolutionMapper {

  public static Solution toEntity(
      SolutionSubmitRequestDTO dto, ErrorReport errorReport, User author) {
    Solution solution = Solution.builder()
        .errorReport(errorReport)
        .probableCause(dto.getProbableCause())
        .explanation(dto.getExplanation())
        .codeChange(dto.getCodeChange())
        .status(SolutionStatus.PENDING)
        .build();
    
    solution.setAuthor(author);
    solution.setLanguage(dto.getLanguage());
    solution.setLanguageVersion(dto.getLanguageVersion());
    solution.setFramework(dto.getFramework());
    solution.setFrameworkVersion(dto.getFrameworkVersion());
    solution.setOs(dto.getOs());
    solution.setOsVersion(dto.getOsVersion());

    return solution;
  }
}
