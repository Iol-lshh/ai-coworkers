package ai.coworkers.repository.sqlite.saga;

import static org.assertj.core.api.Assertions.assertThat;

import ai.coworkers.core.common.UuidV7;
import ai.coworkers.core.model.runtime.saga.Saga;
import ai.coworkers.core.model.runtime.saga.SagaCodec;
import ai.coworkers.core.model.runtime.saga.SagaData;
import ai.coworkers.core.model.runtime.saga.SagaLog;
import ai.coworkers.core.model.runtime.step.slot.StepSlotId;
import ai.coworkers.core.model.runtime.step.slot.parallel.ParallelStepSlot;
import ai.coworkers.core.model.template.step.ParallelStepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.repository.sqlite.InMemorySqlite;
import ai.coworkers.serd.json.saga.SagaJson;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

@DisplayName("SagaLogRepositoryImpl")
class SagaLogRepositoryImplTest {

  private static final SagaCodec CODEC = new SagaCodec();
  private static final SagaJson JSON = new SagaJson();
  private static final StepTemplateId TEMPLATE = StepTemplateId.generate();

  private final JdbcClient jdbcClient = InMemorySqlite.migrated();
  private final SagaLogRepositoryImpl repository =
      new SagaLogRepositoryImpl(jdbcClient, CODEC, JSON);

  private record Row(String logId, String sagaId, SagaData snapshot, String registeredAt) {}

  @Nested
  @DisplayName("append")
  class Append {

    @Test
    @DisplayName("변경마다 로그 식별자, 사가 식별자, 스냅샷, 등록 시각을 한 행씩 추가함")
    void appendsRowPerChange() {
      Saga running =
          Saga.start(
              new ParallelStepSlot(
                  StepSlotId.generate(),
                  new ParallelStepTemplate(TEMPLATE, 1L, List.of(), TEMPLATE),
                  List.of(),
                  StepSlotId.generate()));
      SagaLog first =
          new SagaLog(UuidV7.generate(), running, LocalDateTime.of(2026, 10, 26, 12, 0));
      SagaLog second =
          new SagaLog(UuidV7.generate(), running.end(), LocalDateTime.of(2026, 10, 26, 12, 1));

      repository.append(first);
      repository.append(second);

      List<Row> rows =
          jdbcClient
              .sql("SELECT * FROM saga_log ORDER BY log_id")
              .query(
                  (rs, rowNum) ->
                      new Row(
                          rs.getString("log_id"),
                          rs.getString("saga_id"),
                          JSON.read(rs.getString("snapshot")),
                          rs.getString("registered_at")))
              .list();

      assertThat(rows)
          .containsExactly(
              new Row(
                  first.logId().toString(),
                  running.id().value().toString(),
                  CODEC.encode(running),
                  first.registeredAt().toString()),
              new Row(
                  second.logId().toString(),
                  running.id().value().toString(),
                  CODEC.encode(running.end()),
                  second.registeredAt().toString()));
    }
  }
}
