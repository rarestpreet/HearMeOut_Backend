package com.project.hearmeout_backend.notification_service.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingMessageStore {

  private static final String KEY_PREFIX = "publisher:pending:";
  private static final Duration TTL = Duration.ofMinutes(5);

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  public void put(PendingMessage message) throws JsonProcessingException {
    String key = KEY_PREFIX + message.messageId();
    String value = objectMapper.writeValueAsString(message);
    redisTemplate.opsForValue().set(key, value, TTL);
  }

  public Optional<PendingMessage> get(String messageId) {
    String value = redisTemplate.opsForValue().get(KEY_PREFIX + messageId);
    if (value == null) return Optional.empty();
    try {
      return Optional.of(objectMapper.readValue(value, PendingMessage.class));
    } catch (JacksonException e) {
      log.warn("Error converting json to object: {}\n", e.getMessage(), e);
      return Optional.empty();
    }
  }

  public void remove(String messageId) {
    redisTemplate.delete(KEY_PREFIX + messageId);
  }
}
