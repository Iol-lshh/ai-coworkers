package ai.coworkers.repository.sqlite.step;

import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateCodec;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.log.StepTemplateLog;
import ai.coworkers.core.model.template.step.log.StepTemplateLogRepository;
import ai.coworkers.serd.json.step.StepTemplateJson;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** 템플릿 로그를 한 행씩 추가하고 식별자와 버전으로 스냅샷을 찾음. 스냅샷은 JSON 본문으로 저장함. */
@Repository
@RequiredArgsConstructor
class StepTemplateLogRepositoryImpl implements StepTemplateLogRepository {

  private final JdbcClient jdbcClient;
  private final StepTemplateCodec codec;
  private final StepTemplateJson json;

  @Override
  public void append(StepTemplateLog log) {
    jdbcClient
        .sql(
            "INSERT INTO step_template_log (log_id, template_id, version, snapshot, registered_at)"
                + " VALUES (:logId, :templateId, :version, :snapshot, :registeredAt)")
        .param("logId", log.logId().toString())
        .param("templateId", log.snapshot().id().value().toString())
        .param("version", log.snapshot().version())
        .param("snapshot", json.write(codec.encode(log.snapshot())))
        .param("registeredAt", log.registeredAt().toString())
        .update();
  }

  @Override
  public Optional<StepTemplate> findSnapshot(StepTemplateId id, Long version) {
    return jdbcClient
        .sql(
            "SELECT snapshot FROM step_template_log"
                + " WHERE template_id = :templateId AND version = :version LIMIT 1")
        .param("templateId", id.value().toString())
        .param("version", version)
        .query(String.class)
        .optional()
        .map(snapshot -> codec.decode(json.read(snapshot)));
  }
}
