package com.project.hearmeout_backend.post_service.model;

import com.project.hearmeout_backend.common_lib.model.Post;
import com.project.hearmeout_backend.post_service.model.enums.SolutionStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "solution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solution extends Post {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "error_report_id", nullable = false)
  private ErrorReport errorReport;

  @Column(columnDefinition = "TEXT")
  private String probableCause;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String explanation;

  @Column(columnDefinition = "TEXT")
  private String codeChange;

  @Column(nullable = false)
  private SolutionStatus status;
}
