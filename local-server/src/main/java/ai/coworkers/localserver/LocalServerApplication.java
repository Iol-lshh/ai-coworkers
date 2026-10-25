package ai.coworkers.localserver;

import ai.coworkers.repository.sqlite.SqliteConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SqliteConfiguration.class)
public class LocalServerApplication {

  static void main(String[] args) {
    SpringApplication.run(LocalServerApplication.class, args);
  }
}
