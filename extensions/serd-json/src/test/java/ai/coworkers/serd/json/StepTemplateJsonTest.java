package ai.coworkers.serd.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.template.step.StepTemplateData;
import ai.coworkers.core.model.template.step.StepTemplateData.BranchData;
import ai.coworkers.core.model.template.step.StepTemplateData.LoopData;
import ai.coworkers.core.model.template.step.StepTemplateData.ParallelData;
import ai.coworkers.core.model.template.step.condition.StepConditionData.LeafData;
import ai.coworkers.core.model.template.step.condition.StepConditionData.NotData;
import ai.coworkers.serd.json.step.StepTemplateJson;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("StepTemplateJson")
class StepTemplateJsonTest {

  private static final StepTemplateJson JSON = new StepTemplateJson();

  private static final UUID ID = UUID.fromString("0190a000-0000-7000-8000-000000000001");
  private static final UUID FIRST = UUID.fromString("0190a000-0000-7000-8000-000000000002");
  private static final UUID SECOND = UUID.fromString("0190a000-0000-7000-8000-000000000003");
  private static final NotData CONDITION =
      new NotData(new LeafData("core.present", Map.of("key", "comment")));

  private static final List<StepTemplateData> ALL_KINDS =
      List.of(
          new BranchData(ID, 3L, CONDITION, FIRST, SECOND),
          new LoopData(ID, 3L, CONDITION, FIRST, SECOND),
          new ParallelData(ID, 3L, List.of(FIRST, SECOND), FIRST));

  @Nested
  @DisplayName("write")
  class Write {

    @Test
    @DisplayName("병렬 저장 형태를 종류, 식별 값, 버전, 갈래, 합류를 담은 JSON으로 씀")
    void writesParallel() {
      assertThat(JSON.write(new ParallelData(ID, 3L, List.of(FIRST), SECOND)))
          .isEqualTo(
              "{\"id\":\""
                  + ID
                  + "\",\"version\":3,\"kind\":\"PARALLEL\",\"branches\":[\""
                  + FIRST
                  + "\"],\"join\":\""
                  + SECOND
                  + "\"}");
    }
  }

  @Nested
  @DisplayName("read")
  class Read {

    @Test
    @DisplayName("쓴 JSON을 읽으면 같은 저장 형태가 됨")
    void readsWrittenJson() {
      assertThat(ALL_KINDS)
          .allSatisfy(data -> assertThat(JSON.read(JSON.write(data))).isEqualTo(data));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(
        strings = {
          "not json",
          "[]",
          "{\"id\":\"0190a000-0000-7000-8000-000000000001\",\"version\":1}",
          "{\"id\":\"0190a000-0000-7000-8000-000000000001\",\"version\":1,\"kind\":\"TASK\"}",
          "{\"id\":\"x\",\"version\":1,\"kind\":\"PARALLEL\",\"branches\":[],\"join\":\"x\"}",
          "{\"id\":\"0190a000-0000-7000-8000-000000000001\",\"version\":\"1\",\"kind\":\"PARALLEL\","
              + "\"branches\":[],\"join\":\"0190a000-0000-7000-8000-000000000001\"}"
        })
    @DisplayName("형식이 맞지 않으면 거부함")
    void rejectsMalformedJson(String json) {
      assertThatThrownBy(() -> JSON.read(json))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageStartingWith("invalid template json");
    }

    @Test
    @DisplayName("필드가 빠지면 빠진 필드를 알리며 거부함")
    void rejectsMissingField() {
      String json =
          "{\"id\":\"0190a000-0000-7000-8000-000000000001\",\"version\":1,\"kind\":\"BRANCH\"}";

      assertThatThrownBy(() -> JSON.read(json))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("invalid template json: missing field: condition");
    }
  }
}
