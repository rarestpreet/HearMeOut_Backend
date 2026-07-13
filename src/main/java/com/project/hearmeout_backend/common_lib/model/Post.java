package com.project.hearmeout_backend.common_lib.model;

import com.project.hearmeout_backend.gateway.model.BaseModel;
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

  @Column(nullable = false, length = 20)
  private String language;

  @Column(length = 10)
  private String languageVersion;

  @Column(length = 20)
  private String framework;

  @Column(length = 10)
  private String frameworkVersion;

  @Column(length = 20)
  private String os;

  @Column(length = 10)
  private String osVersion;

  @Column(nullable = false)
  private int score = 0;
}
