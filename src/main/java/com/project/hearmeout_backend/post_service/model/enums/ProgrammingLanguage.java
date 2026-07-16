package com.project.hearmeout_backend.post_service.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProgrammingLanguage {
  JAVASCRIPT("JavaScript"),
  PYTHON("Python"),
  JAVA("Java"),
  TYPESCRIPT("TypeScript"),
  CSHARP("C#"),
  CPLUSPLUS("C++"),
  PHP("PHP"),
  RUBY("Ruby"),
  GO("Go"),
  SWIFT("Swift");

  private final String displayName;

  ProgrammingLanguage(String displayName) {
    this.displayName = displayName;
  }

  @JsonValue
  public String getDisplayName() {
    return displayName;
  }

  @JsonCreator
  public static ProgrammingLanguage fromValue(String value) {
    for (ProgrammingLanguage lang : values()) {
      if (lang.displayName.equalsIgnoreCase(value) || lang.name().equalsIgnoreCase(value)) {
        return lang;
      }
    }
    throw new IllegalArgumentException("Unknown programming language: " + value);
  }
}
