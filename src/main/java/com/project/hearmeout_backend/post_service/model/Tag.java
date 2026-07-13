package com.project.hearmeout_backend.post_service.model;

import com.project.hearmeout_backend.gateway.model.BaseModel;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag extends BaseModel {

  @Column(nullable = false, unique = true, length = 30)
  private String name;

  @Column(length = 200)
  private String description;

  @Builder.Default private int usageCount = 0;

  @ManyToMany(mappedBy = "tags")
  @Builder.Default
  private List<ErrorReport> errorReports = new ArrayList<>();
}
