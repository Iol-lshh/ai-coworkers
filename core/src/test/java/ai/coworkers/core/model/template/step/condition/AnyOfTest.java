package ai.coworkers.core.model.template.step.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.template.step.input.StepInput;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("AnyOf")
class AnyOfTest {

  private static final StepInput INPUT = StepInput.empty();

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @Test
    @DisplayName("자식 조건이 없으면 거부함")
    void rejectsEmptyConditions() {
      assertThatThrownBy(() -> new AnyOf(List.of()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("conditions must not be empty");
    }

    @Test
    @DisplayName("자식 조건 목록을 불변으로 복사함")
    void copiesConditions() {
      List<StepCondition> conditions = new ArrayList<>(List.of(FixedStepConditionType.leaf(true)));
      AnyOf anyOf = new AnyOf(conditions);
      conditions.add(FixedStepConditionType.leaf(false));

      assertThat(anyOf.conditions()).hasSize(1).isUnmodifiable();
    }
  }

  @Nested
  @DisplayName("isSatisfiedBy")
  class IsSatisfiedBy {

    @ParameterizedTest(name = "{0}, {1} → {2}")
    @CsvSource({"true, true, true", "true, false, true", "false, false, false"})
    @DisplayName("자식 조건을 하나 이상 만족하면 만족함")
    void satisfiedWhenAnySatisfied(boolean first, boolean second, boolean expected) {
      AnyOf anyOf =
          new AnyOf(
              List.of(FixedStepConditionType.leaf(first), FixedStepConditionType.leaf(second)));

      assertThat(anyOf.isSatisfiedBy(INPUT)).isEqualTo(expected);
    }
  }
}
