package com.project.hearmeout_backend.post_service.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Framework {
  REACT("React"),
  ANGULAR("Angular"),
  VUE("Vue"),
  EXPRESS("Express"),
  NEXT_JS("Next.js"),
  NODE_JS("Node.js"),
  NEST_JS("NestJS"),
  DJANGO("Django"),
  FLASK("Flask"),
  FASTAPI("FastAPI"),
  SPRING_BOOT("Spring Boot"),
  HIBERNATE("Hibernate"),
  DOT_NET(".NET"),
  ASP_NET_CORE("ASP.NET Core"),
  QT("Qt"),
  UNREAL_ENGINE("Unreal Engine"),
  LARAVEL("Laravel"),
  SYMFONY("Symfony"),
  RUBY_ON_RAILS("Ruby on Rails"),
  SINATRA("Sinatra"),
  GIN("Gin"),
  ECHO("Echo"),
  SWIFTUI("SwiftUI"),
  UIKIT("UIKit");

  private final String displayName;

  Framework(String displayName) {
    this.displayName = displayName;
  }

  @JsonValue
  public String getDisplayName() {
    return displayName;
  }

  @JsonCreator
  public static Framework fromValue(String value) {
    for (Framework fw : values()) {
      if (fw.displayName.equalsIgnoreCase(value) || fw.name().equalsIgnoreCase(value)) {
        return fw;
      }
    }
    throw new IllegalArgumentException("Unknown framework: " + value);
  }
}
