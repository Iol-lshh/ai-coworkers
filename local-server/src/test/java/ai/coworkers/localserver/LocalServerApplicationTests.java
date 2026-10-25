package ai.coworkers.localserver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.context.WebApplicationContext;

/** 공유 인메모리 SQLite로 컨텍스트를 띄움. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:file::memory:?cache=shared")
class LocalServerApplicationTests {

  @Autowired private WebApplicationContext context;

  @Test
  void contextLoads() {}

  @Test
  @DisplayName("OpenAPI 문서에 템플릿 API를 노출함")
  void exposesApiDocs() {
    assertThat(MockMvcTester.from(context).get().uri("/v3/api-docs"))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.paths")
        .asMap()
        .containsKeys("/api/step-templates", "/api/step-templates/{id}");
  }
}
