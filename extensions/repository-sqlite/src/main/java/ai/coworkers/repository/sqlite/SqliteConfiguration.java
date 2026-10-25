package ai.coworkers.repository.sqlite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** SQLite 저장소 설정 */
@Configuration(proxyBeanMethods = false)
public class SqliteConfiguration {

  /**
   * {@code spring.datasource.url}의 DB 파일 디렉터리를 생성하는 후처리기를 등록함.
   *
   * @param environment 스프링 환경
   * @return DB 파일 디렉터리 생성 후처리기
   */
  @Bean
  static SqliteDirectoryInitializer sqliteDirectoryInitializer(Environment environment) {
    return new SqliteDirectoryInitializer(environment.getProperty("spring.datasource.url"));
  }
}
