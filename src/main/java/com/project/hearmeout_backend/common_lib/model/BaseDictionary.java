package com.project.hearmeout_backend.common_lib.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseDictionary extends BaseModel {

  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Column(nullable = false)
  private int usageCount = 0;

  @Column(nullable = false)
  private boolean isApproved = false;
}
