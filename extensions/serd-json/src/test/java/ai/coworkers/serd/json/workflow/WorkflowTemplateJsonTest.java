package ai.coworkers.serd.json.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.template.workflow.WorkflowTemplateData;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("WorkflowTemplateJson")
class WorkflowTemplateJsonTest {

  private static final WorkflowTemplateJson JSON = new WorkflowTemplateJson();
  private static final UUID ID = UUID.fromString("0190a000-0000-7000-8000-000000000001");
  private static final UUID START = UUID.fromString("0190a000-0000-7000-8000-000000000002");
  private static final UUID NEXT = UUID.fromString("0190a000-0000-7000-8000-000000000003");
  private static final WorkflowTemplateData DATA =
      new WorkflowTemplateData(ID, 2L, START, List.of(START, NEXT));

  @Nested
  @DisplayName("write")
  class Write {

    @Test
    @DisplayName("식별 값, 버전, 시작 템플릿, 템플릿 목록을 담은 JSON으로 씀")
    void writesFields() {
      assertThat(JSON.write(DATA))
          .isEqualTo(
              "{\"id\":\""
                  + ID
                  + "\",\"version\":2,\"start\":\""
                  + START
                  + "\",\"steps\":[\""
                  + START
                  + "\",\""
                  + NEXT
                  + "\"]}");
    }
  }

  @Nested
  @DisplayName("read")
  class Read {

    @Test
    @DisplayName("쓴 JSON을 읽으면 같은 저장 형태가 됨")
    void readsWrittenJson() {
      assertThat(JSON.read(JSON.write(DATA))).isEqualTo(DATA);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(
        strings = {
          "not json",
          "[]",
          "{\"id\":\"0190a000-0000-7000-8000-000000000001\",\"version\":1,"
              + "\"start\":\"0190a000-0000-7000-8000-000000000001\"}",
          "{\"id\":\"x\",\"version\":1,\"start\":\"x\",\"steps\":[]}"
        })
    @DisplayName("형식이 맞지 않으면 거부함")
    void rejectsInvalidJson(String json) {
      assertThatThrownBy(() -> JSON.read(json))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageStartingWith("invalid workflow template json");
    }
  }
}
