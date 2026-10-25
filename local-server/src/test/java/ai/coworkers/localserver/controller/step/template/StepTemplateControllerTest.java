package ai.coworkers.localserver.controller.step.template;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.web.context.WebApplicationContext;

@DisplayName("StepTemplateController")
@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:file::memory:?cache=shared")
class StepTemplateControllerTest {

  private static final String URI = "/api/step-templates";
  private static final String NEXT = "0190a000-0000-7000-8000-000000000001";
  private static final String CONDITION =
      "{\"kind\":\"LEAF\",\"code\":\"core.bool\",\"config\":{\"key\":\"approved\",\"expected\":\"true\"}}";
  private static final String BRANCH =
      "{\"kind\":\"BRANCH\",\"condition\":"
          + CONDITION
          + ",\"whenSatisfied\":\""
          + NEXT
          + "\",\"otherwise\":\""
          + NEXT
          + "\"}";
  private static final String PARALLEL =
      "{\"kind\":\"PARALLEL\",\"branches\":[\"" + NEXT + "\"],\"join\":\"" + NEXT + "\"}";

  @Autowired private WebApplicationContext context;
  @Autowired private JdbcClient jdbcClient;

  private MockMvcTester mvc;

  @BeforeEach
  void setUp() {
    mvc = MockMvcTester.from(context);
    jdbcClient.sql("DELETE FROM step_template").update();
    jdbcClient.sql("DELETE FROM step_template_log").update();
  }

  private MvcTestResult post(String body) {
    return mvc.post().uri(URI).contentType(MediaType.APPLICATION_JSON).content(body).exchange();
  }

  private String create(String body) {
    try {
      return JsonPath.read(
          post(body).getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id");
    } catch (UnsupportedEncodingException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("첫 버전으로 등록하고 201과 현재본을 응답함")
    void respondsCreated() {
      assertThat(post(BRANCH))
          .hasStatus(HttpStatus.CREATED)
          .bodyJson()
          .isLenientlyEqualTo(
              "{\"kind\":\"BRANCH\",\"version\":1,\"condition\":"
                  + CONDITION
                  + ",\"whenSatisfied\":\""
                  + NEXT
                  + "\",\"otherwise\":\""
                  + NEXT
                  + "\"}");
    }

    @Test
    @DisplayName("필수 필드가 없으면 400으로 응답함")
    void rejectsMissingField() {
      assertThat(post("{\"kind\":\"PARALLEL\",\"join\":\"" + NEXT + "\"}"))
          .hasStatus(HttpStatus.BAD_REQUEST)
          .bodyJson()
          .extractingPath("$.detail")
          .isEqualTo("missing field: branches");
    }

    @Test
    @DisplayName("등록되지 않은 조건 종류를 쓰면 400으로 응답함")
    void rejectsUnknownConditionType() {
      assertThat(post(BRANCH.replace("core.bool", "test.unknown")))
          .hasStatus(HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("현재본을 응답함")
    void respondsCurrent() {
      String id = create(PARALLEL);

      assertThat(mvc.get().uri(URI + "/" + id))
          .hasStatusOk()
          .bodyJson()
          .isLenientlyEqualTo(
              "{\"id\":\""
                  + id
                  + "\",\"kind\":\"PARALLEL\",\"version\":1,\"branches\":[\""
                  + NEXT
                  + "\"],\"join\":\""
                  + NEXT
                  + "\"}");
    }

    @Test
    @DisplayName("현재본이 없으면 404로 응답함")
    void respondsNotFound() {
      assertThat(mvc.get().uri(URI + "/" + UUID.randomUUID())).hasStatus(HttpStatus.NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("page")
  class PageMethod {

    @Test
    @DisplayName("전체 개수와 해당 페이지의 현재본을 응답함")
    void respondsPage() {
      create(PARALLEL);
      create(PARALLEL);
      create(BRANCH);

      assertThat(mvc.get().uri(URI).param("page", "1").param("size", "2"))
          .hasStatusOk()
          .bodyJson()
          .isLenientlyEqualTo("{\"total\":3}")
          .extractingPath("$.list.length()")
          .isEqualTo(1);
    }

    @Test
    @DisplayName("페이지 크기가 1보다 작으면 400으로 응답함")
    void rejectsInvalidSize() {
      assertThat(mvc.get().uri(URI).param("size", "0")).hasStatus(HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    @DisplayName("기준 버전이 현재본과 같으면 수정하고 올린 버전을 응답함")
    void respondsUpdated() {
      String id = create(PARALLEL);

      assertThat(
              mvc.put()
                  .uri(URI + "/" + id)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"expectedVersion\":1,\"definition\":" + BRANCH + "}"))
          .hasStatusOk()
          .bodyJson()
          .isLenientlyEqualTo("{\"id\":\"" + id + "\",\"kind\":\"BRANCH\",\"version\":2}");
    }

    @Test
    @DisplayName("기준 버전이 현재본과 다르면 409로 응답함")
    void respondsConflict() {
      String id = create(PARALLEL);

      assertThat(
              mvc.put()
                  .uri(URI + "/" + id)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"expectedVersion\":2,\"definition\":" + PARALLEL + "}"))
          .hasStatus(HttpStatus.CONFLICT);
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("현재본을 삭제하고 204로 응답함")
    void respondsNoContent() {
      String id = create(PARALLEL);

      assertThat(mvc.delete().uri(URI + "/" + id)).hasStatus(HttpStatus.NO_CONTENT);
      assertThat(mvc.get().uri(URI + "/" + id)).hasStatus(HttpStatus.NOT_FOUND);
    }
  }
}
