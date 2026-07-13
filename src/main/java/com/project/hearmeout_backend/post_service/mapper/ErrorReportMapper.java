package com.project.hearmeout_backend.post_service.mapper;

import com.project.hearmeout_backend.post_service.dto.request.ErrorReportSubmitRequestDTO;
import com.project.hearmeout_backend.post_service.model.ErrorReport;
import com.project.hearmeout_backend.post_service.model.Tag;
import com.project.hearmeout_backend.post_service.model.enums.ErrorReportStatus;
import com.project.hearmeout_backend.user_service.model.User;
import java.util.List;

public class ErrorReportMapper {

  public static ErrorReport toEntity(ErrorReportSubmitRequestDTO dto, User author, List<Tag> tags) {
    ErrorReport report =
        ErrorReport.builder()
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
            .status(ErrorReportStatus.OPEN)
            .tags(tags)
            .build();

    report.setAuthor(author);
    report.setLanguage(dto.getLanguage());
    report.setLanguageVersion(dto.getLanguageVersion());
    report.setFramework(dto.getFramework());
    report.setFrameworkVersion(dto.getFrameworkVersion());
    report.setOs(dto.getOs());
    report.setOsVersion(dto.getOsVersion());

    return report;
  }
}
