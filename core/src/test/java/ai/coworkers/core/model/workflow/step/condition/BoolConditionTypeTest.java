package ai.coworkers.core.model.workflow.step.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.input.StepInputValue.BoolValue;
import ai.coworkers.core.model.workflow.step.input.StepInputValue.TextValue;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("BoolConditionType")
class BoolConditionTypeTest {

  private static final BoolConditionType TYPE = new BoolConditionType();
  private static final StepInput INPUT =
      new StepInput(Map.of("approved", new BoolValue(true), "comment", new TextValue("true")));

  @Nested
  @DisplayName("validate")
  class Validate {

    @Test
    @DisplayName("값 이름이 없으면 거부함")
    void rejectsMissingKey() {
      assertThatThrownBy(() -> TYPE.validate(Map.of("expected", "true")))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("missing condition config: key");
    }

    @ParameterizedTest(name = "expected [{0}]")
    @ValueSource(strings = {"TRUE", "yes", "1"})
    @DisplayName("기대값이 true, false가 아니면 거부함")
    void rejectsInvalidExpected(String expected) {
      assertThatThrownBy(() -> TYPE.validate(Map.of("key", "approved", "expected", expected)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("invalid condition config: expected=" + expected);
    }
  }

  @Nested
  @DisplayName("isSatisfiedBy")
  class IsSatisfiedBy {

    @ParameterizedTest(name = "key={0}, expected={1} → {2}")
    @CsvSource({
      "approved, true, true",
      "approved, false, false",
      "comment, true, false",
      "unknown, true, false"
    })
    @DisplayName("논리값이 기대값과 같을 때만 만족함")
    void satisfiedWhenEqual(String key, String expected, boolean satisfied) {
      assertThat(TYPE.isSatisfiedBy(Map.of("key", key, "expected", expected), INPUT))
          .isEqualTo(satisfied);
    }
  }
}
