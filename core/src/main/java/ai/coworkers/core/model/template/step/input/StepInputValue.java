package ai.coworkers.core.model.template.step.input;

import java.math.BigDecimal;

/** 단계 입력에 담기는 값 */
public sealed interface StepInputValue {

  /**
   * 논리값
   *
   * @param value 값
   */
  record BoolValue(boolean value) implements StepInputValue {}

  /**
   * 숫자값
   *
   * @param value 값
   */
  record NumberValue(BigDecimal value) implements StepInputValue {

    /**
     * 정수로 숫자값을 만듦.
     *
     * @param value 값
     * @return 숫자값
     */
    public static NumberValue of(long value) {
      return new NumberValue(BigDecimal.valueOf(value));
    }

    /**
     * 실수의 십진 표기로 숫자값을 만듦. 유한하지 않으면 거부함.
     *
     * @param value 값
     * @return 숫자값
     */
    public static NumberValue of(double value) {
      if (!Double.isFinite(value)) {
        throw new IllegalArgumentException("number value must be finite: " + value);
      }
      return new NumberValue(BigDecimal.valueOf(value));
    }
  }

  /**
   * 문자열값
   *
   * @param value 값
   */
  record TextValue(String value) implements StepInputValue {}
}
