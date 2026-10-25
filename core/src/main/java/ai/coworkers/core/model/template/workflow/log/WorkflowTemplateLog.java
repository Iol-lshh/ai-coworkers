package ai.coworkers.core.model.template.workflow.log;

import ai.coworkers.core.common.HistoryLog;
import ai.coworkers.core.model.template.workflow.WorkflowTemplate;
import java.time.LocalDateTime;
import java.util.UUID;

public record WorkflowTemplateLog(UUID logId, WorkflowTemplate snapshot, LocalDateTime registeredAt)
    implements HistoryLog<WorkflowTemplate> {}
