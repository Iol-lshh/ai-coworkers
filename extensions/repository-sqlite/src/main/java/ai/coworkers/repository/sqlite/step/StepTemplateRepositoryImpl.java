package ai.coworkers.repository.sqlite.step;

import ai.coworkers.core.common.Page;
import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateCodec;
import ai.coworkers.core.model.template.step.StepTemplateCommand;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.StepTemplateRepository;
import ai.coworkers.serd.json.step.StepTemplateJson;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** 템플릿 현재본을 식별자 한 행에 JSON 본문으로 저장함. 버전 조건부 갱신으로 낙관적 잠금함. */
@Repository
@RequiredArgsConstructor
class StepTemplateRepositoryImpl implements StepTemplateRepository {

  private final JdbcClient jdbcClient;
  private final StepTemplateCodec codec;
  private final StepTemplateJson json;

  @Override
  public Optional<StepTemplate> findById(StepTemplateId id) {
    return jdbcClient
        .sql("SELECT body FROM step_template WHERE id = :id")
        .param("id", id.value().toString())
        .query(String.class)
        .optional()
        .map(this::toTemplate);
  }

  @Override
  public Page<StepTemplate> find(StepTemplateCommand.FindPage command) {
    long total = jdbcClient.sql("SELECT COUNT(*) FROM step_template").query(Long.class).single();
    List<StepTemplate> content =
        jdbcClient
            .sql("SELECT body FROM step_template ORDER BY id LIMIT :size OFFSET :offset")
            .param("size", command.size())
            .param("offset", command.page() * command.size())
            .query((rs, rowNum) -> toTemplate(rs.getString("body")))
            .list();
    return new Page<>(total, content);
  }

  @Override
  public boolean existsById(StepTemplateId id) {
    return jdbcClient
            .sql("SELECT COUNT(*) FROM step_template WHERE id = :id")
            .param("id", id.value().toString())
            .query(Long.class)
            .single()
        > 0;
  }

  /** 직전 버전 행을 갱신하고, 갱신할 행이 없으면 새로 추가함. 같은 식별자의 다른 버전 행이 있으면 충돌로 거부함. */
  @Override
  public void save(StepTemplate template) {
    String id = template.id().value().toString();
    String body = json.write(codec.encode(template));
    int updated =
        jdbcClient
            .sql(
                "UPDATE step_template SET version = :version, body = :body"
                    + " WHERE id = :id AND version = :previous")
            .param("version", template.version())
            .param("body", body)
            .param("id", id)
            .param("previous", template.version() - 1)
            .update();
    if (updated == 1) {
      return;
    }
    if (existsById(template.id())) {
      throw new IllegalStateException(
          "stale template version: " + template.id() + " v" + template.version());
    }
    jdbcClient
        .sql("INSERT INTO step_template (id, version, body) VALUES (:id, :version, :body)")
        .param("id", id)
        .param("version", template.version())
        .param("body", body)
        .update();
  }

  @Override
  public void deleteById(StepTemplateId id) {
    jdbcClient
        .sql("DELETE FROM step_template WHERE id = :id")
        .param("id", id.value().toString())
        .update();
  }

  private StepTemplate toTemplate(String body) {
    return codec.decode(json.read(body));
  }
}
