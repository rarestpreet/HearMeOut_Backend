package com.project.hearmeout_backend.common_lib.utils;

public class StringFormatter {
  public static String capitalizeWords(String str) {
    if (str == null || str.trim().isEmpty()) {
      return str;
    }
    String[] words = str.trim().split("\\s+");
    StringBuilder capitalized = new StringBuilder();
    for (String word : words) {
      if (!word.isEmpty()) {
        capitalized
            .append(Character.toUpperCase(word.charAt(0)))
            .append(word.substring(1).toLowerCase())
            .append(" ");
      }
    }
    return capitalized.toString().trim();
  }
}
