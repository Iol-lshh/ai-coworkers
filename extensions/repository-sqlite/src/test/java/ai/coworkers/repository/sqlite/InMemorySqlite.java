package ai.coworkers.repository.sqlite;

import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/** 마이그레이션을 적용한 테스트용 인메모리 SQLite. 연결 하나를 유지해 DB가 사라지지 않게 함. */
public final class InMemorySqlite {

  private InMemorySqlite() {}

  /**
   * 마이그레이션을 적용한 새 인메모리 DB의 JDBC 클라이언트를 만듦.
   *
   * @return JDBC 클라이언트
   */
  public static JdbcClient migrated() {
    SingleConnectionDataSource dataSource =
        new SingleConnectionDataSource("jdbc:sqlite::memory:", true);
    Flyway.configure().dataSource(dataSource).load().migrate();
    return JdbcClient.create(dataSource);
  }
}
