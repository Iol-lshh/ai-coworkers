package ai.coworkers.core.model.template.step.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.template.step.input.StepInput;
import ai.coworkers.core.model.template.step.input.StepInputValue.NumberValue;
import ai.coworkers.core.model.template.step.input.StepInputValue.TextValue;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("NumberConditionType")
class NumberConditionTypeTest {

  private static final NumberConditionType TYPE = new NumberConditionType();
  private static final StepInput INPUT =
      new StepInput(Map.of("score", NumberValue.of(70.0), "label", new TextValue("70")));

  @Nested
  @DisplayName("validate")
  class Validate {

    @ParameterizedTest(name = "operator [{0}]")
    @ValueSource(strings = {"gte", ">=", "BETWEEN"})
    @DisplayName("연산자가 정의되지 않았으면 거부함")
    void rejectsInvalidOperator(String operator) {
      assertThatThrownBy(
              () -> TYPE.validate(Map.of("key", "score", "operator", operator, "value", "70")))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("invalid condition config: operator=" + operator);
    }

    @ParameterizedTest(name = "value [{0}]")
    @ValueSource(strings = {"seventy", "7O", "1,000"})
    @DisplayName("기준값이 숫자가 아니면 거부함")
    void rejectsInvalidValue(String value) {
      assertThatThrownBy(
              () -> TYPE.validate(Map.of("key", "score", "operator", "GTE", "value", value)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("invalid condition config: value=" + value);
    }
  }

  @Nested
  @DisplayName("isSatisfiedBy")
  class IsSatisfiedBy {

    @ParameterizedTest(name = "70.0 {0} {1} → {2}")
    @CsvSource({
      "EQ, 70, true",
      "EQ, 71, false",
      "NE, 71, true",
      "NE, 70, false",
      "GT, 69, true",
      "GT, 70, false",
      "GTE, 70, true",
      "GTE, 71, false",
      "LT, 71, true",
      "LT, 70, false",
      "LTE, 70, true",
      "LTE, 69, false"
    })
    @DisplayName("숫자값을 기준값과 연산자로 비교함")
    void comparesWithOperator(String operator, String value, boolean expected) {
      assertThat(
              TYPE.isSatisfiedBy(
                  Map.of("key", "score", "operator", operator, "value", value), INPUT))
          .isEqualTo(expected);
    }

    @ParameterizedTest(name = "key={0}")
    @ValueSource(strings = {"label", "unknown"})
    @DisplayName("값이 없거나 숫자값이 아니면 만족하지 않음")
    void unsatisfiedWithoutNumber(String key) {
      assertThat(TYPE.isSatisfiedBy(Map.of("key", key, "operator", "GTE", "value", "0"), INPUT))
          .isFalse();
    }
  }
}
