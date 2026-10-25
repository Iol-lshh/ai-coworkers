package ai.coworkers.serd.json.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.runtime.step.slot.StepSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.BranchSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.LoopSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.ParallelSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.TaskSlotData;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("StepSlotJson")
class StepSlotJsonTest {

  private static final StepSlotJson JSON = new StepSlotJson();
  private static final JsonMapper MAPPER = JsonMapper.builder().build();
  private static final UUID ID = UUID.fromString("0190a000-0000-7000-8000-000000000001");
  private static final UUID TEMPLATE = UUID.fromString("0190a000-0000-7000-8000-000000000002");
  private static final UUID FIRST = UUID.fromString("0190a000-0000-7000-8000-000000000003");
  private static final UUID SECOND = UUID.fromString("0190a000-0000-7000-8000-000000000004");

  private static final List<StepSlotData> ALL_KINDS =
      List.of(
          new TaskSlotData(ID, TEMPLATE, 2L, FIRST),
          new TaskSlotData(ID, TEMPLATE, 2L, null),
          new BranchSlotData(ID, TEMPLATE, 2L, FIRST, SECOND),
          new LoopSlotData(ID, TEMPLATE, 2L, FIRST, SECOND),
          new ParallelSlotData(ID, TEMPLATE, 2L, List.of(FIRST, SECOND), FIRST));

  @Nested
  @DisplayName("toNode")
  class ToNode {

    @Test
    @DisplayName("다음 슬롯이 없는 작업 슬롯은 다음 슬롯을 널로 씀")
    void writesNullNext() {
      assertThat(JSON.toNode(new TaskSlotData(ID, TEMPLATE, 2L, null)).toString())
          .isEqualTo(
              "{\"id\":\""
                  + ID
                  + "\",\"templateId\":\""
                  + TEMPLATE
                  + "\",\"templateVersion\":2,\"kind\":\"TASK\",\"next\":null}");
    }
  }

  @Nested
  @DisplayName("fromNode")
  class FromNode {

    @Test
    @DisplayName("쓴 노드를 읽으면 같은 저장 형태가 됨")
    void readsWrittenNode() {
      assertThat(ALL_KINDS)
          .allSatisfy(data -> assertThat(JSON.fromNode(JSON.toNode(data))).isEqualTo(data));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(
        strings = {
          "[]",
          "{\"id\":\"0190a000-0000-7000-8000-000000000001\","
              + "\"templateId\":\"0190a000-0000-7000-8000-000000000001\","
              + "\"templateVersion\":1,\"kind\":\"TASK\"}",
          "{\"id\":\"0190a000-0000-7000-8000-000000000001\","
              + "\"templateId\":\"0190a000-0000-7000-8000-000000000001\","
              + "\"templateVersion\":1,\"kind\":\"REVIEW\"}"
        })
    @DisplayName("형식이 맞지 않으면 거부함")
    void rejectsInvalidNode(String json) {
      assertThatThrownBy(() -> JSON.fromNode(MAPPER.readTree(json)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageStartingWith("invalid slot json");
    }
  }
}
