package com.project.hearmeout_backend.post_service.service;

import java.util.List;

public interface DictionaryService {
  List<String> getTopValues(String type, String search, int limit);
}
