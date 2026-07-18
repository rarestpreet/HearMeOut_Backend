package com.project.hearmeout_backend.common_lib.model;

import com.project.hearmeout_backend.post_service.model.Framework;
import com.project.hearmeout_backend.post_service.model.OperatingSystem;
import com.project.hearmeout_backend.post_service.model.ProgrammingLanguage;
import com.project.hearmeout_backend.user_service.model.User;
import jakarta.persistence.Column;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "language_id", nullable = false)
  private ProgrammingLanguage language;

  @Column(length = 10)
  private String languageVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "framework_id")
  private Framework framework;

  @Column(length = 10)
  private String frameworkVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "os_id")
  private OperatingSystem os;

  @Column(length = 10)
  private String osVersion;

  @Column(nullable = false)
  private int score = 0;
}
