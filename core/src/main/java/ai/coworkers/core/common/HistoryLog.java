package ai.coworkers.core.common;

import java.time.LocalDateTime;
import java.util.UUID;

public interface HistoryLog<T> {
  UUID logId();

  T snapshot();

  LocalDateTime registeredAt();
}
