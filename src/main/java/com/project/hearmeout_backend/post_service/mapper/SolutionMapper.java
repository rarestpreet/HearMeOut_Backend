package com.project.hearmeout_backend.post_service.mapper;

import com.project.hearmeout_backend.post_service.dto.request.SolutionSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.model.ErrorReport;
import com.project.hearmeout_backend.post_service.model.Solution;
import com.project.hearmeout_backend.post_service.model.enums.SolutionStatus;
import com.project.hearmeout_backend.user_service.model.User;

public class SolutionMapper {

  public static Solution toEntity(
      SolutionSubmitRequestDTO dto, ErrorReport errorReport, User author) {
    return Solution.builder()
        .errorReport(errorReport)
        .probableCause(dto.getProbableCause())
        .explanation(dto.getExplanation())
        .codeChange(dto.getCodeChange())
        .language(dto.getLanguage())
        .languageVersion(dto.getLanguageVersion())
        .framework(dto.getFramework())
        .frameworkVersion(dto.getFrameworkVersion())
        .os(dto.getOs())
        .osVersion(dto.getOsVersion())
        .status(SolutionStatus.PENDING)
        .build();
  }
}
