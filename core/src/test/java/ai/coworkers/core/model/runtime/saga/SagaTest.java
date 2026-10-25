package ai.coworkers.core.model.runtime.saga;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.runtime.step.node.StepNode;
import ai.coworkers.core.model.runtime.step.node.StepNodeState;
import ai.coworkers.core.model.runtime.step.output.StepOutput;
import ai.coworkers.core.model.runtime.step.slot.StepSlot;
import ai.coworkers.core.model.runtime.step.slot.StepSlotId;
import ai.coworkers.core.model.template.step.ParallelStepTemplate;
import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.input.StepInput;
import java.util.List;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("Saga")
class SagaTest {

  private static final StepTemplate TEMPLATE =
      new ParallelStepTemplate(StepTemplateId.generate(), 1L, List.of(), StepTemplateId.generate());

  private record TestSlot(StepSlotId id, StepTemplate template) implements StepSlot {
    @Override
    public StepOutput process(StepInput input) {
      throw new AssertionError("process must not be called");
    }
  }

  private static final StepSlot FIRST = new TestSlot(StepSlotId.generate(), TEMPLATE);
  private static final StepSlot SECOND = new TestSlot(StepSlotId.generate(), TEMPLATE);

  private static Saga inState(SagaState state) {
    Saga running = Saga.start(FIRST);
    return switch (state) {
      case RUNNING -> running;
      case WAITING -> running.await();
      case JOINED -> running.join();
      case ENDED -> running.end();
    };
  }

  private static void assertRejectsUnlessRunning(SagaState state, UnaryOperator<Saga> transition) {
    assertThatThrownBy(() -> transition.apply(inState(state)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageEndingWith("is " + state + ", expected RUNNING");
  }

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @ParameterizedTest(name = "{0}")
    @EnumSource(SagaState.class)
    @DisplayName("상태와 현재 노드 유무가 맞지 않으면 거부함")
    void rejectsStateMismatch(SagaState state) {
      StepNode mismatched = state.isFinished() ? Saga.start(FIRST).activeNode() : null;

      assertThatThrownBy(() -> new Saga(SagaId.generate(), state, mismatched))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("saga state mismatch with active node: " + state);
    }
  }

  @Nested
  @DisplayName("start")
  class Start {

    @Test
    @DisplayName("슬롯에 도착한 노드를 가리키는 진행 상태의 사가를 만듦")
    void startsRunningAtArrivedNode() {
      Saga saga = Saga.start(FIRST);

      assertThat(saga.state()).isSameAs(SagaState.RUNNING);
      assertThat(saga.activeNode())
          .satisfies(node -> assertThat(node.stepSlot()).isSameAs(FIRST))
          .satisfies(node -> assertThat(node.nodeState()).isSameAs(StepNodeState.ARRIVED));
    }
  }

  @Nested
  @DisplayName("moveTo")
  class MoveTo {

    @Test
    @DisplayName("같은 사가가 다음 슬롯의 새 노드를 가리킴")
    void movesToNewNode() {
      Saga saga = Saga.start(FIRST);

      Saga moved = saga.moveTo(SECOND);

      assertThat(moved.id()).isEqualTo(saga.id());
      assertThat(moved.activeNode())
          .satisfies(node -> assertThat(node.stepSlot()).isSameAs(SECOND))
          .satisfies(node -> assertThat(node.id()).isNotEqualTo(saga.activeNode().id()));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = SagaState.class, names = "RUNNING", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("진행 상태가 아니면 거부함")
    void rejectsNotRunning(SagaState state) {
      assertRejectsUnlessRunning(state, saga -> saga.moveTo(SECOND));
    }
  }

  @Nested
  @DisplayName("await")
  class Await {

    @Test
    @DisplayName("현재 노드를 유지한 채 대기 상태로 바꿈")
    void waitsAtCurrentNode() {
      Saga saga = Saga.start(FIRST);

      assertThat(saga.await()).isEqualTo(new Saga(saga.id(), SagaState.WAITING, saga.activeNode()));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = SagaState.class, names = "RUNNING", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("진행 상태가 아니면 거부함")
    void rejectsNotRunning(SagaState state) {
      assertRejectsUnlessRunning(state, Saga::await);
    }
  }

  @Nested
  @DisplayName("resume")
  class Resume {

    @Test
    @DisplayName("현재 노드를 유지한 채 진행 상태로 되돌림")
    void resumesAtCurrentNode() {
      Saga saga = Saga.start(FIRST);

      assertThat(saga.await().resume()).isEqualTo(saga);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = SagaState.class, names = "WAITING", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("대기 상태가 아니면 거부함")
    void rejectsNotWaiting(SagaState state) {
      assertThatThrownBy(() -> inState(state).resume())
          .isInstanceOf(IllegalStateException.class)
          .hasMessageEndingWith("is " + state + ", expected WAITING");
    }
  }

  @Nested
  @DisplayName("join")
  class Join {

    @Test
    @DisplayName("현재 노드 없이 합류 상태로 끝냄")
    void finishesAsJoined() {
      Saga saga = Saga.start(FIRST);

      assertThat(saga.join()).isEqualTo(new Saga(saga.id(), SagaState.JOINED, null));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = SagaState.class, names = "RUNNING", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("진행 상태가 아니면 거부함")
    void rejectsNotRunning(SagaState state) {
      assertRejectsUnlessRunning(state, Saga::join);
    }
  }

  @Nested
  @DisplayName("end")
  class End {

    @Test
    @DisplayName("현재 노드 없이 종료 상태로 끝냄")
    void finishesAsEnded() {
      Saga saga = Saga.start(FIRST);

      assertThat(saga.end()).isEqualTo(new Saga(saga.id(), SagaState.ENDED, null));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = SagaState.class, names = "RUNNING", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("진행 상태가 아니면 거부함")
    void rejectsNotRunning(SagaState state) {
      assertRejectsUnlessRunning(state, Saga::end);
    }
  }
}
