package com.project.hearmeout_backend.post_service.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OperatingSystem {
  WINDOWS("Windows"),
  MACOS("macOS"),
  LINUX("Linux");

  private final String displayName;

  OperatingSystem(String displayName) {
    this.displayName = displayName;
  }

  @JsonValue
  public String getDisplayName() {
    return displayName;
  }

  @JsonCreator
  public static OperatingSystem fromValue(String value) {
    for (OperatingSystem os : values()) {
      if (os.displayName.equalsIgnoreCase(value) || os.name().equalsIgnoreCase(value)) {
        return os;
      }
    }
    throw new IllegalArgumentException("Unknown operating system: " + value);
  }
}
