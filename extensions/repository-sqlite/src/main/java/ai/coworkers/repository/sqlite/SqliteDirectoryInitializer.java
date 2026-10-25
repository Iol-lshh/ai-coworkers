package ai.coworkers.repository.sqlite;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.BeanPostProcessor;

/** DataSource 초기화 전에 SQLite DB 파일의 상위 디렉터리를 생성하는 후처리기 */
@RequiredArgsConstructor
class SqliteDirectoryInitializer implements BeanPostProcessor {

  private static final String URL_PREFIX = "jdbc:sqlite:";
  private static final String FILE_PREFIX = "file:";
  private static final String MEMORY = ":memory:";

  private final @Nullable String url;

  /** DataSource 빈이면 DB 파일의 상위 디렉터리를 생성함. */
  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) {
    if (bean instanceof DataSource) {
      databaseDirectory(url).ifPresent(SqliteDirectoryInitializer::createDirectories);
    }
    return bean;
  }

  /**
   * JDBC URL에서 SQLite DB 파일의 상위 디렉터리를 추출함.
   *
   * @param url JDBC URL
   * @return SQLite 파일 DB면 상위 디렉터리, 아니면 빈 값
   */
  static Optional<Path> databaseDirectory(@Nullable String url) {
    if (url == null || !url.startsWith(URL_PREFIX)) {
      return Optional.empty();
    }
    String location = url.substring(URL_PREFIX.length());
    int query = location.indexOf('?');
    if (query >= 0) {
      location = location.substring(0, query);
    }
    if (location.startsWith(FILE_PREFIX)) {
      location = location.substring(FILE_PREFIX.length());
    }
    if (location.isBlank() || location.contains(MEMORY)) {
      return Optional.empty();
    }
    return Optional.ofNullable(Path.of(location).toAbsolutePath().getParent());
  }

  private static void createDirectories(Path directory) {
    try {
      Files.createDirectories(directory);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
