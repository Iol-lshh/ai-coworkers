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

@DisplayName("TextConditionType")
class TextConditionTypeTest {

  private static final TextConditionType TYPE = new TextConditionType();
  private static final StepInput INPUT =
      new StepInput(Map.of("choice", new TextValue("  approve "), "flag", new BoolValue(true)));

  @Nested
  @DisplayName("validate")
  class Validate {

    @Test
    @DisplayName("기준값이 없으면 거부함")
    void rejectsMissingValue() {
      assertThatThrownBy(() -> TYPE.validate(Map.of("key", "choice")))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("missing condition config: value");
    }
  }

  @Nested
  @DisplayName("isSatisfiedBy")
  class IsSatisfiedBy {

    @ParameterizedTest(name = "key={0}, value=[{1}] → {2}")
    @CsvSource({
      "choice, approve, true",
      "choice, ' approve', true",
      "choice, Approve, false",
      "choice, reject, false",
      "flag, true, false",
      "unknown, approve, false"
    })
    @DisplayName("앞뒤 공백을 제거한 문자열값이 기준값과 같을 때만 만족함")
    void satisfiedWhenEqual(String key, String value, boolean expected) {
      assertThat(TYPE.isSatisfiedBy(Map.of("key", key, "value", value), INPUT)).isEqualTo(expected);
    }
  }
}
