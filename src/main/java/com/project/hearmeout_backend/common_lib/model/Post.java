package com.project.hearmeout_backend.common_lib.model;

import com.project.hearmeout_backend.post_service.model.enums.Framework;
import com.project.hearmeout_backend.post_service.model.enums.OperatingSystem;
import com.project.hearmeout_backend.post_service.model.enums.ProgrammingLanguage;
import com.project.hearmeout_backend.user_service.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public abstract class Post extends BaseModel {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ProgrammingLanguage language;

  @Column(length = 10)
  private String languageVersion;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private Framework framework;

  @Column(length = 10)
  private String frameworkVersion;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private OperatingSystem os;

  @Column(length = 10)
  private String osVersion;

  @Column(nullable = false)
  private int score = 0;
}
