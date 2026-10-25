package ai.coworkers.repository.sqlite.saga;

import ai.coworkers.core.model.runtime.saga.SagaCodec;
import ai.coworkers.core.model.runtime.saga.SagaLog;
import ai.coworkers.core.model.runtime.saga.SagaLogRepository;
import ai.coworkers.serd.json.saga.SagaJson;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** 사가 로그를 한 행씩 추가함. 스냅샷은 JSON 본문으로 저장함. */
@Repository
@RequiredArgsConstructor
class SagaLogRepositoryImpl implements SagaLogRepository {

  private final JdbcClient jdbcClient;
  private final SagaCodec codec;
  private final SagaJson json;

  @Override
  public void append(SagaLog log) {
    jdbcClient
        .sql(
            "INSERT INTO saga_log (log_id, saga_id, snapshot, registered_at)"
                + " VALUES (:logId, :sagaId, :snapshot, :registeredAt)")
        .param("logId", log.logId().toString())
        .param("sagaId", log.snapshot().id().value().toString())
        .param("snapshot", json.write(codec.encode(log.snapshot())))
        .param("registeredAt", log.registeredAt().toString())
        .update();
  }
}
