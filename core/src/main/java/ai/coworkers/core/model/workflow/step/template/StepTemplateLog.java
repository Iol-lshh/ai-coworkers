package ai.coworkers.core.model.workflow.step.template;

import ai.coworkers.core.common.HistoryLog;
import java.time.LocalDateTime;
import java.util.UUID;

public record StepTemplateLog(UUID logId, StepTemplate snapshot, LocalDateTime registeredAt)
    implements HistoryLog<StepTemplate> {}
