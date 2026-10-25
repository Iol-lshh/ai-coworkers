package ai.coworkers.serd.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.workflow.step.condition.StepConditionData;
import ai.coworkers.core.model.workflow.step.condition.StepConditionData.AllOfData;
import ai.coworkers.core.model.workflow.step.condition.StepConditionData.AnyOfData;
import ai.coworkers.core.model.workflow.step.condition.StepConditionData.LeafData;
import ai.coworkers.core.model.workflow.step.condition.StepConditionData.NotData;
import java.util.List;
import java.util.Map;

import ai.coworkers.serd.json.step.StepConditionJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("StepConditionJson")
class StepConditionJsonTest {

  private static final StepConditionJson JSON = new StepConditionJson();

  private static final StepConditionData DATA =
      new AllOfData(
          List.of(
              new LeafData("core.bool", Map.of("key", "approved")),
              new AnyOfData(List.of(new NotData(new LeafData("core.present", Map.of()))))));

  private static final String TEXT =
      """
      {"kind":"ALL_OF","children":[\
      {"kind":"LEAF","code":"core.bool","config":{"key":"approved"}},\
      {"kind":"ANY_OF","children":[\
      {"kind":"NOT","child":{"kind":"LEAF","code":"core.present","config":{}}}]}]}""";

  @Nested
  @DisplayName("write")
  class Write {

    @Test
    @DisplayName("저장 형태를 kind로 종류를 구분한 JSON으로 바꿈")
    void writesJson() {
      assertThat(JSON.write(DATA)).isEqualTo(TEXT);
    }
  }

  @Nested
  @DisplayName("read")
  class Read {

    @Test
    @DisplayName("JSON을 같은 구조의 저장 형태로 바꿈")
    void readsJson() {
      assertThat(JSON.read(TEXT)).isEqualTo(DATA);
    }

    @Test
    @DisplayName("쓴 JSON을 읽으면 원래 저장 형태가 됨")
    void roundTrips() {
      StepConditionData data =
          new LeafData("core.number", Map.of("key", "score", "operator", "GTE", "value", "70"));

      assertThat(JSON.read(JSON.write(data))).isEqualTo(data);
    }

    @Test
    @DisplayName("JSON 문법이 틀리면 거부함")
    void rejectsMalformedJson() {
      assertThatThrownBy(() -> JSON.read("{\"kind\":"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("invalid condition json");
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource(
        delimiter = '|',
        value = {
          "[]                                          | node must be an object",
          "{}                                          | missing field: kind",
          "{\"kind\":1}                                | kind must be a string",
          "{\"kind\":\"OR\"}                           | unknown kind: OR",
          "{\"kind\":\"ALL_OF\"}                       | missing field: children",
          "{\"kind\":\"ANY_OF\",\"children\":{}}       | children must be an array",
          "{\"kind\":\"NOT\"}                          | missing field: child",
          "{\"kind\":\"LEAF\",\"config\":{}}           | missing field: code",
          "{\"kind\":\"LEAF\",\"code\":\"c\"}          | missing field: config",
          "{\"kind\":\"LEAF\",\"code\":\"c\",\"config\":[]}           | config must be an object",
          "{\"kind\":\"LEAF\",\"code\":\"c\",\"config\":{\"key\":1}}  | config.key must be a string"
        })
    @DisplayName("형식이 맞지 않으면 이유와 함께 거부함")
    void rejectsInvalidShape(String json, String reason) {
      assertThatThrownBy(() -> JSON.read(json))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("invalid condition json: " + reason);
    }
  }
}
