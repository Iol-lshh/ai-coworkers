package ai.coworkers.core.model.runtime.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.runtime.saga.SagaId;
import ai.coworkers.core.model.runtime.step.slot.StepSlotId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SagaJoin")
class SagaJoinTest {

  private static final SagaId ORIGIN = SagaId.generate();
  private static final SagaId FIRST = SagaId.generate();
  private static final SagaId SECOND = SagaId.generate();
  private static final StepSlotId JOIN = StepSlotId.generate();
  private static final SagaJoin WAITING =
      new SagaJoin(ORIGIN, List.of(FIRST, SECOND), JOIN, Set.of());

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @Test
    @DisplayName("갈래가 아닌 사가가 도착해 있으면 거부함")
    void rejectsJoinedOutsideBranches() {
      assertThatThrownBy(() -> new SagaJoin(ORIGIN, List.of(FIRST), JOIN, Set.of(SECOND)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("joined saga not in branches: " + Set.of(SECOND));
    }
  }

  @Nested
  @DisplayName("hasBranch")
  class HasBranch {

    @Test
    @DisplayName("갈래 사가이면 참, 대기한 사가이면 거짓을 반환함")
    void distinguishesBranchFromOrigin() {
      assertThat(WAITING.hasBranch(FIRST)).isTrue();
      assertThat(WAITING.hasBranch(ORIGIN)).isFalse();
    }
  }

  @Nested
  @DisplayName("arrive")
  class Arrive {

    @Test
    @DisplayName("도착한 갈래를 기록한 합류 대기를 반환함")
    void recordsArrival() {
      assertThat(WAITING.arrive(FIRST))
          .isEqualTo(new SagaJoin(ORIGIN, List.of(FIRST, SECOND), JOIN, Set.of(FIRST)));
    }

    @Test
    @DisplayName("갈래가 아닌 사가의 도착은 거부함")
    void rejectsNonBranch() {
      assertThatThrownBy(() -> WAITING.arrive(ORIGIN)).isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("isComplete")
  class IsComplete {

    @Test
    @DisplayName("갈래가 모두 도착해야 참을 반환함")
    void completesWhenAllArrive() {
      assertThat(WAITING.arrive(FIRST).isComplete()).isFalse();
      assertThat(WAITING.arrive(FIRST).arrive(SECOND).isComplete()).isTrue();
    }
  }
}
