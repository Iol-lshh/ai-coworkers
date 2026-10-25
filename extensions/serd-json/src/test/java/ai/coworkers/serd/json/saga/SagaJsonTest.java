package ai.coworkers.serd.json.saga;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.runtime.saga.SagaData;
import ai.coworkers.core.model.runtime.saga.SagaData.NodeData;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("SagaJson")
class SagaJsonTest {

  private static final SagaJson JSON = new SagaJson();
  private static final UUID ID = UUID.fromString("0190a000-0000-7000-8000-000000000001");
  private static final UUID NODE = UUID.fromString("0190a000-0000-7000-8000-000000000002");
  private static final UUID SLOT = UUID.fromString("0190a000-0000-7000-8000-000000000003");

  @Nested
  @DisplayName("write")
  class Write {

    @Test
    @DisplayName("식별 값, 상태, 노드를 담은 JSON으로 씀")
    void writesWithNode() {
      assertThat(JSON.write(new SagaData(ID, "RUNNING", new NodeData(NODE, "ARRIVED", SLOT))))
          .isEqualTo(
              "{\"id\":\""
                  + ID
                  + "\",\"state\":\"RUNNING\",\"node\":{\"id\":\""
                  + NODE
                  + "\",\"state\":\"ARRIVED\",\"slotId\":\""
                  + SLOT
                  + "\"}}");
    }

    @Test
    @DisplayName("노드가 없으면 널로 씀")
    void writesNullNode() {
      assertThat(JSON.write(new SagaData(ID, "ENDED", null)))
          .isEqualTo("{\"id\":\"" + ID + "\",\"state\":\"ENDED\",\"node\":null}");
    }
  }

  @Nested
  @DisplayName("read")
  class Read {

    @Test
    @DisplayName("쓴 JSON을 읽으면 같은 저장 형태가 됨")
    void readsWrittenJson() {
      assertThat(
              List.of(
                  new SagaData(ID, "WAITING", new NodeData(NODE, "ARRIVED", SLOT)),
                  new SagaData(ID, "JOINED", null)))
          .allSatisfy(data -> assertThat(JSON.read(JSON.write(data))).isEqualTo(data));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(
        strings = {
          "not json",
          "{\"id\":\"0190a000-0000-7000-8000-000000000001\",\"state\":\"ENDED\"}",
          "{\"id\":\"0190a000-0000-7000-8000-000000000001\",\"state\":\"RUNNING\",\"node\":[]}",
          "{\"id\":\"0190a000-0000-7000-8000-000000000001\",\"state\":1,\"node\":null}"
        })
    @DisplayName("형식이 맞지 않으면 거부함")
    void rejectsInvalidJson(String json) {
      assertThatThrownBy(() -> JSON.read(json))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageStartingWith("invalid saga json");
    }
  }
}
