package ai.coworkers.core.model.runtime.saga;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.runtime.saga.SagaData.NodeData;
import ai.coworkers.core.model.runtime.step.slot.StepSlot;
import ai.coworkers.core.model.runtime.step.slot.StepSlotId;
import ai.coworkers.core.model.runtime.step.slot.parallel.ParallelStepSlot;
import ai.coworkers.core.model.template.step.ParallelStepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("SagaCodec")
class SagaCodecTest {

  private static final SagaCodec CODEC = new SagaCodec();
  private static final StepTemplateId JOIN = StepTemplateId.generate();
  private static final StepSlot SLOT =
      new ParallelStepSlot(
          StepSlotId.generate(),
          new ParallelStepTemplate(StepTemplateId.generate(), 1L, List.of(), JOIN),
          List.of(),
          StepSlotId.generate());
  private static final Function<StepSlotId, StepSlot> SLOT_OF =
      id -> {
        if (!id.equals(SLOT.id())) {
          throw new IllegalArgumentException("unknown slot id: " + id);
        }
        return SLOT;
      };

  private static Saga inState(SagaState state) {
    Saga running = Saga.start(SLOT);
    return switch (state) {
      case RUNNING -> running;
      case WAITING -> running.await();
      case JOINED -> running.join();
      case ENDED -> running.end();
    };
  }

  @Nested
  @DisplayName("encode")
  class Encode {

    @Test
    @DisplayName("상태 이름과 노드의 식별 값·상태 이름·슬롯 식별 값을 담음")
    void encodesWithNode() {
      Saga saga = Saga.start(SLOT);
      var node = Objects.requireNonNull(saga.activeNode());

      assertThat(CODEC.encode(saga))
          .isEqualTo(
              new SagaData(
                  saga.id().value(),
                  "RUNNING",
                  new NodeData(node.id().value(), "ARRIVED", SLOT.id().value())));
    }

    @Test
    @DisplayName("끝난 사가는 노드 없이 담음")
    void encodesWithoutNode() {
      Saga saga = Saga.start(SLOT).end();

      assertThat(CODEC.encode(saga)).isEqualTo(new SagaData(saga.id().value(), "ENDED", null));
    }
  }

  @Nested
  @DisplayName("decode")
  class Decode {

    @ParameterizedTest(name = "{0}")
    @EnumSource(SagaState.class)
    @DisplayName("저장 형태와 슬롯으로 같은 사가를 복원함")
    void decodesEncoded(SagaState state) {
      Saga saga = inState(state);

      assertThat(CODEC.decode(CODEC.encode(saga), SLOT_OF)).isEqualTo(saga);
    }

    @Test
    @DisplayName("모르는 상태 이름이면 거부함")
    void rejectsUnknownState() {
      SagaData data = new SagaData(UUID.randomUUID(), "PAUSED", null);

      assertThatThrownBy(() -> CODEC.decode(data, SLOT_OF))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("unknown SagaState: PAUSED");
    }

    @Test
    @DisplayName("상태와 노드 유무가 맞지 않으면 거부함")
    void rejectsRuleViolation() {
      SagaData data = new SagaData(UUID.randomUUID(), "RUNNING", null);

      assertThatThrownBy(() -> CODEC.decode(data, SLOT_OF))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("saga state mismatch with active node: RUNNING");
    }
  }
}
