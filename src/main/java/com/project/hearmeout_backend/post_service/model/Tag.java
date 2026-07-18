package com.project.hearmeout_backend.post_service.model;

import com.project.hearmeout_backend.common_lib.model.BaseDictionary;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "tag")
@Getter
@Setter
@NoArgsConstructor
public class Tag extends BaseDictionary {

  @Column(length = 200)
  private String description;

  @Builder
  public Tag(
      String name,
      int usageCount,
      boolean isApproved,
      String description,
      List<ErrorReport> errorReports) {
    super(name, usageCount, isApproved);
    this.description = description;
    if (errorReports != null) {
      this.errorReports = errorReports;
    }
  }

  @ManyToMany(mappedBy = "tags")
  private List<ErrorReport> errorReports = new ArrayList<>();
}
