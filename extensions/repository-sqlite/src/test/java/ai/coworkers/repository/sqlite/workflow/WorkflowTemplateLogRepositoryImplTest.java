package ai.coworkers.repository.sqlite.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import ai.coworkers.core.common.UuidV7;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.workflow.WorkflowTemplate;
import ai.coworkers.core.model.template.workflow.WorkflowTemplateCodec;
import ai.coworkers.core.model.template.workflow.WorkflowTemplateId;
import ai.coworkers.core.model.template.workflow.log.WorkflowTemplateLog;
import ai.coworkers.repository.sqlite.InMemorySqlite;
import ai.coworkers.serd.json.workflow.WorkflowTemplateJson;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

@DisplayName("WorkflowTemplateLogRepositoryImpl")
class WorkflowTemplateLogRepositoryImplTest {

  private static final WorkflowTemplateCodec CODEC = new WorkflowTemplateCodec();
  private static final WorkflowTemplateJson JSON = new WorkflowTemplateJson();
  private static final WorkflowTemplateId ID = WorkflowTemplateId.generate();
  private static final StepTemplateId START = StepTemplateId.generate();

  private final JdbcClient jdbcClient = InMemorySqlite.migrated();
  private final WorkflowTemplateLogRepositoryImpl repository =
      new WorkflowTemplateLogRepositoryImpl(jdbcClient, CODEC, JSON);

  private static WorkflowTemplateLog log(Long version) {
    return new WorkflowTemplateLog(
        UuidV7.generate(),
        new WorkflowTemplate(ID, version, START, List.of(START)),
        LocalDateTime.of(2026, 10, 26, 12, 0, version.intValue()));
  }

  @Nested
  @DisplayName("append")
  class Append {

    @Test
    @DisplayName("버전마다 식별자, 버전, 복원 가능한 스냅샷, 등록 시각을 한 행씩 추가함")
    void appendsRowPerVersion() {
      WorkflowTemplateLog first = log(1L);
      WorkflowTemplateLog second = log(2L);

      repository.append(first);
      repository.append(second);

      List<WorkflowTemplateLog> stored =
          jdbcClient
              .sql("SELECT * FROM workflow_template_log ORDER BY version")
              .query(
                  (rs, rowNum) ->
                      new WorkflowTemplateLog(
                          UUID.fromString(rs.getString("log_id")),
                          CODEC.decode(JSON.read(rs.getString("snapshot"))),
                          LocalDateTime.parse(rs.getString("registered_at"))))
              .list();
      List<Map<String, Object>> keys =
          jdbcClient
              .sql("SELECT template_id, version FROM workflow_template_log ORDER BY version")
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
