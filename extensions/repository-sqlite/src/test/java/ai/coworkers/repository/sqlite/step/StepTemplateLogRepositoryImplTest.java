package ai.coworkers.repository.sqlite.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import ai.coworkers.core.common.UuidV7;
import ai.coworkers.core.model.workflow.step.condition.StepConditionCodec;
import ai.coworkers.core.model.workflow.step.template.ParallelStepTemplate;
import ai.coworkers.core.model.workflow.step.template.StepTemplateCodec;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import ai.coworkers.core.model.workflow.step.template.StepTemplateLog;
import ai.coworkers.repository.sqlite.InMemorySqlite;
import ai.coworkers.serd.json.step.StepTemplateJson;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

@DisplayName("StepTemplateLogRepositoryImpl")
class StepTemplateLogRepositoryImplTest {

  private static final StepTemplateCodec CODEC = new StepTemplateCodec(new StepConditionCodec());
  private static final StepTemplateJson JSON = new StepTemplateJson();
  private static final StepTemplateId ID = StepTemplateId.generate();

  private final JdbcClient jdbcClient = InMemorySqlite.migrated();
  private final StepTemplateLogRepositoryImpl repository =
      new StepTemplateLogRepositoryImpl(jdbcClient, CODEC, JSON);

  private static StepTemplateLog log(Long version) {
    return new StepTemplateLog(
        UuidV7.generate(),
        new ParallelStepTemplate(ID, version, List.of(), ID),
        LocalDateTime.of(2026, 10, 25, 12, 0, version.intValue()));
  }

  @Nested
  @DisplayName("append")
  class Append {

    @Test
    @DisplayName("버전마다 식별자, 버전, 복원 가능한 스냅샷, 등록 시각을 한 행씩 추가함")
    void appendsRowPerVersion() {
      StepTemplateLog first = log(1L);
      StepTemplateLog second = log(2L);

      repository.append(first);
      repository.append(second);

      List<StepTemplateLog> stored =
          jdbcClient
              .sql("SELECT * FROM step_template_log ORDER BY version")
              .query(
                  (rs, rowNum) ->
                      new StepTemplateLog(
                          UUID.fromString(rs.getString("log_id")),
                          CODEC.decode(JSON.read(rs.getString("snapshot"))),
                          LocalDateTime.parse(rs.getString("registered_at"))))
              .list();
      List<Map<String, Object>> keys =
          jdbcClient
              .sql("SELECT template_id, version FROM step_template_log ORDER BY version")
              .query()
              .listOfRows();

      assertThat(stored).containsExactly(first, second);
      assertThat(keys)
          .extracting(
              row -> row.get("template_id"), row -> ((Number) row.get("version")).longValue())
          .containsExactly(tuple(ID.value().toString(), 1L), tuple(ID.value().toString(), 2L));
    }
  }
}
