package com.project.hearmeout_backend.post_service.model;

import com.project.hearmeout_backend.common_lib.model.Post;
import com.project.hearmeout_backend.post_service.model.enums.ErrorReportStatus;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "error_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorReport extends Post {

  @Column(nullable = false, length = 100)
  private String title;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String description;

  @Column(columnDefinition = "TEXT")
  private String reproductionSteps;

  @Column(nullable = false, length = 50)
  private String errorType;

  @Column(length = 200)
  private String repositoryUrl;

  @Column(length = 100)
  private String branch;

  @Column(length = 100)
  private String commitHash;

  @Column(length = 200)
  private String filePath;

  @Column(columnDefinition = "TEXT")
  private String relevantCode;

  @Column(columnDefinition = "TEXT")
  private String relevantLog;

  @Column(nullable = false)
  private ErrorReportStatus status;

  @Builder.Default private int viewCount = 0;

  @OneToMany(mappedBy = "errorReport", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Solution> solutions = new ArrayList<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "error_report_tag",
      joinColumns = @JoinColumn(name = "error_report_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  @Builder.Default
  private List<Tag> tags = new ArrayList<>();
}
