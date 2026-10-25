package ai.coworkers.serd.json.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.runtime.saga.SagaData;
import ai.coworkers.core.model.runtime.saga.SagaData.NodeData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.ParallelSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.TaskSlotData;
import ai.coworkers.core.model.runtime.workflow.WorkflowData;
import ai.coworkers.core.model.runtime.workflow.WorkflowData.SagaJoinData;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("WorkflowJson")
class WorkflowJsonTest {

  private static final WorkflowJson JSON = new WorkflowJson();
  private static final UUID ID = UUID.fromString("0190a000-0000-7000-8000-000000000001");
  private static final UUID TEMPLATE = UUID.fromString("0190a000-0000-7000-8000-000000000002");
  private static final UUID PARALLEL = UUID.fromString("0190a000-0000-7000-8000-000000000003");
  private static final UUID BRANCH = UUID.fromString("0190a000-0000-7000-8000-000000000004");
  private static final UUID ORIGIN = UUID.fromString("0190a000-0000-7000-8000-000000000005");
  private static final UUID SAGA = UUID.fromString("0190a000-0000-7000-8000-000000000006");
  private static final UUID NODE = UUID.fromString("0190a000-0000-7000-8000-000000000007");

  private static final WorkflowData DATA =
      new WorkflowData(
          ID,
          TEMPLATE,
          3L,
          List.of(
              new ParallelSlotData(PARALLEL, TEMPLATE, 1L, List.of(BRANCH), BRANCH),
              new TaskSlotData(BRANCH, TEMPLATE, 1L, null)),
          List.of(
              new SagaData(ORIGIN, "WAITING", new NodeData(NODE, "ARRIVED", PARALLEL)),
              new SagaData(SAGA, "JOINED", null)),
          List.of(new SagaJoinData(ORIGIN, List.of(SAGA), BRANCH, List.of(SAGA))));

  @Nested
  @DisplayName("write")
  class Write {

    @Test
    @DisplayName("합류 대기를 대기한 사가, 갈래, 합류 슬롯, 도착한 갈래로 씀")
    void writesJoins() {
      assertThat(JSON.write(DATA))
          .endsWith(
              "\"joins\":[{\"origin\":\""
                  + ORIGIN
                  + "\",\"branches\":[\""
                  + SAGA
                  + "\"],\"join\":\""
                  + BRANCH
                  + "\",\"joined\":[\""
                  + SAGA
                  + "\"]}]}");
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
          "{\"id\":\"0190a000-0000-7000-8000-000000000001\","
              + "\"templateId\":\"0190a000-0000-7000-8000-000000000001\","
              + "\"templateVersion\":1,\"slots\":[],\"sagas\":[]}",
          "{\"id\":\"0190a000-0000-7000-8000-000000000001\","
              + "\"templateId\":\"0190a000-0000-7000-8000-000000000001\","
              + "\"templateVersion\":1,\"slots\":[],\"sagas\":[],\"joins\":[1]}"
        })
    @DisplayName("형식이 맞지 않으면 거부함")
    void rejectsInvalidJson(String json) {
      assertThatThrownBy(() -> JSON.read(json))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageStartingWith("invalid workflow json");
    }
  }
}
