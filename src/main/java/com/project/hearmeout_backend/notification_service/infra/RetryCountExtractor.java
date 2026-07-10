package com.project.hearmeout_backend.notification_service.infra;

import java.util.List;
import java.util.Map;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

@Component
public class RetryCountExtractor {

  public long getRetryCount(Message message) {
    Map<String, Object> headers = message.getMessageProperties().getHeaders();
    List<Map<String, Object>> xDeath = (List<Map<String, Object>>) headers.get("x-death");
    if (xDeath == null || xDeath.isEmpty()) return 0;
    return xDeath.stream().mapToLong(d -> (Long) d.get("count")).sum();
  }
}
