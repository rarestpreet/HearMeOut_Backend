package com.project.hearmeout_backend.user_service.model;

import com.project.hearmeout_backend.common_lib.model.BaseDictionary;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profession")
@Getter
@Setter
@NoArgsConstructor
public class Profession extends BaseDictionary {

  @Builder
  public Profession(String name, int usageCount, boolean isApproved) {
    super(name, usageCount, isApproved);
  }
}
