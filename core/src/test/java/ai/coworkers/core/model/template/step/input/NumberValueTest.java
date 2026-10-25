package ai.coworkers.core.model.template.step.input;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.template.step.input.StepInputValue.NumberValue;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("NumberValue")
class NumberValueTest {

  @Nested
  @DisplayName("of")
  class Of {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({"70, 70", "-3, -3", "0, 0"})
    @DisplayName("정수로 같은 값의 숫자값을 만듦")
    void createsFromLong(long value, String expected) {
      assertThat(NumberValue.of(value).value()).isEqualTo(new BigDecimal(expected));
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({"0.1, 0.1", "70.5, 70.5", "70.0, 70.0"})
    @DisplayName("실수의 십진 표기로 숫자값을 만듦")
    void createsFromDouble(double value, String expected) {
      assertThat(NumberValue.of(value).value()).isEqualTo(new BigDecimal(expected));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    @DisplayName("유한하지 않은 실수면 거부함")
    void rejectsNonFinite(double value) {
      assertThatThrownBy(() -> NumberValue.of(value))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("number value must be finite: " + value);
    }
  }
}
