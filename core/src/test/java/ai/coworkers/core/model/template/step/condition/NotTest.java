package ai.coworkers.core.model.template.step.condition;

import static org.assertj.core.api.Assertions.assertThat;

import ai.coworkers.core.model.template.step.input.StepInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Not")
class NotTest {

  private static final StepInput INPUT = StepInput.empty();

  @Nested
  @DisplayName("isSatisfiedBy")
  class IsSatisfiedBy {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({"true, false", "false, true"})
    @DisplayName("자식 조건의 판정을 반전함")
    void negatesCondition(boolean satisfied, boolean expected) {
      Not not = new Not(FixedStepConditionType.leaf(satisfied));

      assertThat(not.isSatisfiedBy(INPUT)).isEqualTo(expected);
    }
  }
}
