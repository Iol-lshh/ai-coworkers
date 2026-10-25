package ai.coworkers.core.model.workflow.saga;

import ai.coworkers.core.common.HistoryLog;
import java.time.LocalDateTime;
import java.util.UUID;

public record SagaLog(UUID logId, Saga snapshot, LocalDateTime registeredAt)
    implements HistoryLog<Saga> {}
