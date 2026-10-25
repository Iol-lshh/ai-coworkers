package ai.coworkers.core.model.workflow.step.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.workflow.step.input.StepInput;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("AllOf")
class AllOfTest {

  private static final StepInput INPUT = StepInput.empty();

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @Test
    @DisplayName("자식 조건이 없으면 거부함")
    void rejectsEmptyConditions() {
      assertThatThrownBy(() -> new AllOf(List.of()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("conditions must not be empty");
    }

    @Test
    @DisplayName("자식 조건 목록을 불변으로 복사함")
    void copiesConditions() {
      List<StepCondition> conditions = new ArrayList<>(List.of(FixedStepConditionType.leaf(true)));
      AllOf allOf = new AllOf(conditions);
      conditions.add(FixedStepConditionType.leaf(false));

      assertThat(allOf.conditions()).hasSize(1).isUnmodifiable();
    }
  }

  @Nested
  @DisplayName("isSatisfiedBy")
  class IsSatisfiedBy {

    @ParameterizedTest(name = "{0}, {1} → {2}")
    @CsvSource({"true, true, true", "true, false, false", "false, false, false"})
    @DisplayName("자식 조건을 모두 만족할 때만 만족함")
    void satisfiedWhenAllSatisfied(boolean first, boolean second, boolean expected) {
      AllOf allOf =
          new AllOf(
              List.of(FixedStepConditionType.leaf(first), FixedStepConditionType.leaf(second)));

      assertThat(allOf.isSatisfiedBy(INPUT)).isEqualTo(expected);
    }
  }
}
