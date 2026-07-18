package com.project.hearmeout_backend.post_service.service.implementation;

import com.project.hearmeout_backend.post_service.repository.FrameworkRepository;
import com.project.hearmeout_backend.post_service.repository.OperatingSystemRepository;
import com.project.hearmeout_backend.post_service.repository.ProgrammingLanguageRepository;
import com.project.hearmeout_backend.post_service.service.DictionaryService;
import com.project.hearmeout_backend.user_service.repository.ProfessionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {

  private final ProfessionRepository professionRepo;
  private final FrameworkRepository frameworkRepo;
  private final OperatingSystemRepository osRepo;
  private final ProgrammingLanguageRepository languageRepo;

  @Override
  public List<String> getTopValues(String type, String search, int limit) {
    PageRequest pageRequest = PageRequest.of(0, limit);
    return switch (type.toLowerCase()) {
      case "profession" -> professionRepo.findTopValues(search, pageRequest);
      case "framework" -> frameworkRepo.findTopValues(search, pageRequest);
      case "os" -> osRepo.findTopValues(search, pageRequest);
      case "language" -> languageRepo.findTopValues(search, pageRequest);
      default -> throw new IllegalArgumentException("Unknown dictionary type: " + type);
    };
  }
}
