package com.project.hearmeout_backend.post_service.model;

import com.project.hearmeout_backend.common_lib.model.BaseDictionary;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "programming_language")
@Getter
@Setter
@NoArgsConstructor
public class ProgrammingLanguage extends BaseDictionary {

  @Builder
  public ProgrammingLanguage(String name, int usageCount, boolean isApproved) {
    super(name, usageCount, isApproved);
  }
}
