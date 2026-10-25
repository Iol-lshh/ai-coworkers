package ai.coworkers.core.model.template.step.log;

import ai.coworkers.core.common.HistoryLog;
import ai.coworkers.core.model.template.step.StepTemplate;
import java.time.LocalDateTime;
import java.util.UUID;

public record StepTemplateLog(UUID logId, StepTemplate snapshot, LocalDateTime registeredAt)
    implements HistoryLog<StepTemplate> {}
