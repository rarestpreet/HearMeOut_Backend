package com.project.hearmeout_backend.post_service.mapper;

import com.project.hearmeout_backend.post_service.dto.request.ErrorReportSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.model.ErrorReport;
import com.project.hearmeout_backend.post_service.model.Tag;
import com.project.hearmeout_backend.post_service.model.enums.ErrorReportStatus;
import com.project.hearmeout_backend.user_service.model.User;
import java.util.List;

public class ErrorReportMapper {

  public static ErrorReport toEntity(
      ErrorReportSubmitRequestDTO dto, User author, List<Tag> tags) {
    return ErrorReport.builder()
        .title(dto.getTitle())
        .description(dto.getDescription())
        .reproductionSteps(dto.getReproductionSteps())
        .errorType(dto.getErrorType())
        .repositoryUrl(dto.getRepositoryUrl())
        .branch(dto.getBranch())
        .commitHash(dto.getCommitHash())
        .filePath(dto.getFilePath())
        .relevantCode(dto.getRelevantCode())
        .relevantLog(dto.getRelevantLog())
        .language(dto.getLanguage())
        .languageVersion(dto.getLanguageVersion())
        .framework(dto.getFramework())
        .frameworkVersion(dto.getFrameworkVersion())
        .os(dto.getOs())
        .osVersion(dto.getOsVersion())
        .status(ErrorReportStatus.OPEN)
        .tags(tags)
        .build();
  }
}
