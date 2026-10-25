package ai.coworkers.core.model.workflow.step.node;

import static org.assertj.core.api.Assertions.assertThat;

import ai.coworkers.core.model.workflow.step.slot.StepSlotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("StepNodeState")
class StepNodeStateTest {

  @Nested
  @DisplayName("slotState")
  class SlotState {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
      "ARRIVED, PENDING",
      "READY, PENDING",
      "RUNNING, RUNNING",
      "SUCCEEDED, RESOLVED",
      "FAILED, RESOLVED",
      "CANCELLED, RESOLVED"
    })
    @DisplayName("노드 상태에 대응하는 슬롯 상태를 반환함")
    void returnsMappedSlotState(StepNodeState state, StepSlotState expected) {
      assertThat(state.slotState()).isSameAs(expected);
    }
  }
}
