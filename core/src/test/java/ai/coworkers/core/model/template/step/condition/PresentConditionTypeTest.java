package ai.coworkers.core.model.template.step.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.template.step.input.StepInput;
import ai.coworkers.core.model.template.step.input.StepInputValue.TextValue;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("PresentConditionType")
class PresentConditionTypeTest {

  private static final PresentConditionType TYPE = new PresentConditionType();
  private static final StepInput INPUT = new StepInput(Map.of("comment", new TextValue("ok")));

  @Nested
  @DisplayName("validate")
  class Validate {

    @ParameterizedTest(name = "key [{0}]")
    @ValueSource(strings = {"", " "})
    @DisplayName("값 이름이 비어 있으면 거부함")
    void rejectsBlankKey(String key) {
      assertThatThrownBy(() -> TYPE.validate(Map.of("key", key)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("missing condition config: key");
    }
  }

  @Nested
  @DisplayName("isSatisfiedBy")
  class IsSatisfiedBy {

    @ParameterizedTest(name = "key={0} → {1}")
    @CsvSource({"comment, true", "unknown, false"})
    @DisplayName("입력에 값이 있으면 만족함")
    void satisfiedWhenPresent(String key, boolean expected) {
      assertThat(TYPE.isSatisfiedBy(Map.of("key", key), INPUT)).isEqualTo(expected);
    }
  }
}
