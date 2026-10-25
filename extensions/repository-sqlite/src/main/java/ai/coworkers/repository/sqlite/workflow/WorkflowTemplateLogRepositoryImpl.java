package ai.coworkers.repository.sqlite.workflow;

import ai.coworkers.core.model.template.workflow.WorkflowTemplateCodec;
import ai.coworkers.core.model.template.workflow.log.WorkflowTemplateLog;
import ai.coworkers.core.model.template.workflow.log.WorkflowTemplateLogRepository;
import ai.coworkers.serd.json.workflow.WorkflowTemplateJson;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** 워크플로우 틀 로그를 한 행씩 추가함. 스냅샷은 JSON 본문으로 저장함. */
@Repository
@RequiredArgsConstructor
class WorkflowTemplateLogRepositoryImpl implements WorkflowTemplateLogRepository {

  private final JdbcClient jdbcClient;
  private final WorkflowTemplateCodec codec;
  private final WorkflowTemplateJson json;

  @Override
  public void append(WorkflowTemplateLog log) {
    jdbcClient
        .sql(
            "INSERT INTO workflow_template_log"
                + " (log_id, template_id, version, snapshot, registered_at)"
                + " VALUES (:logId, :templateId, :version, :snapshot, :registeredAt)")
        .param("logId", log.logId().toString())
        .param("templateId", log.snapshot().id().value().toString())
        .param("version", log.snapshot().version())
        .param("snapshot", json.write(codec.encode(log.snapshot())))
        .param("registeredAt", log.registeredAt().toString())
        .update();
  }
}
