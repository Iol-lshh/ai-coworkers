package ai.coworkers.repository.sqlite.workflow;

import ai.coworkers.core.model.runtime.workflow.Workflow;
import ai.coworkers.core.model.runtime.workflow.WorkflowCodec;
import ai.coworkers.core.model.runtime.workflow.WorkflowId;
import ai.coworkers.core.model.runtime.workflow.WorkflowRepository;
import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.log.StepTemplateLogRepository;
import ai.coworkers.serd.json.workflow.WorkflowJson;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** 워크플로우 실행을 식별자 한 행에 JSON 본문으로 저장함. 읽을 때 슬롯의 템플릿은 템플릿 로그의 스냅샷으로 복원함. */
@Repository
@RequiredArgsConstructor
class WorkflowRepositoryImpl implements WorkflowRepository {

  private final JdbcClient jdbcClient;
  private final WorkflowCodec codec;
  private final WorkflowJson json;
  private final StepTemplateLogRepository templateLogs;

  @Override
  public Optional<Workflow> findById(WorkflowId id) {
    return jdbcClient
        .sql("SELECT body FROM workflow WHERE id = :id")
        .param("id", id.value().toString())
        .query(String.class)
        .optional()
        .map(body -> codec.decode(json.read(body), this::snapshotOf));
  }

  /** 같은 식별자의 행이 있으면 본문을 갱신하고, 없으면 새로 추가함. */
  @Override
  public void save(Workflow workflow) {
    jdbcClient
        .sql(
            "INSERT INTO workflow (id, template_id, template_version, body)"
                + " VALUES (:id, :templateId, :templateVersion, :body)"
                + " ON CONFLICT (id) DO UPDATE SET body = excluded.body")
        .param("id", workflow.id().value().toString())
        .param("templateId", workflow.templateId().value().toString())
        .param("templateVersion", workflow.templateVersion())
        .param("body", json.write(codec.encode(workflow)))
        .update();
  }

  private StepTemplate snapshotOf(StepTemplateId id, Long version) {
    return templateLogs
        .findSnapshot(id, version)
        .orElseThrow(
            () -> new NoSuchElementException("unknown template snapshot: " + id + " v" + version));
  }
}
