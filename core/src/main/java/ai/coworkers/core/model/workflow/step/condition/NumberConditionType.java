package ai.coworkers.core.model.workflow.step.condition;

import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.input.StepInputValue.NumberValue;
import java.math.BigDecimal;
import java.util.Map;
import java.util.function.IntPredicate;

/**
 * 입력의 숫자값을 기준값과 비교하는 조건 종류. 설정값은 {@code key}, {@code operator}, {@code value}. 값이 없거나 숫자값이 아니면 만족하지
 * 않음.
 */
record NumberConditionType() implements StepConditionType {

  private static final String OPERATOR = "operator";
  private static final String VALUE = "value";

  /** 입력값과 기준값의 비교 연산 */
  enum Operator {
    EQ(result -> result == 0),
    NE(result -> result != 0),
    GT(result -> result > 0),
    GTE(result -> result >= 0),
    LT(result -> result < 0),
    LTE(result -> result <= 0);

    private final IntPredicate comparison;

    Operator(IntPredicate comparison) {
      this.comparison = comparison;
    }

    boolean test(BigDecimal actual, BigDecimal expected) {
      return comparison.test(actual.compareTo(expected));
    }
  }

  @Override
  public String code() {
    return "core.number";
  }

  @Override
  public void validate(Map<String, String> config) {
    ConditionConfig.require(config, ConditionConfig.KEY);
    ConditionConfig.requireEnum(config, OPERATOR, Operator.class);
    ConditionConfig.requireNumber(config, VALUE);
  }

  @Override
  public boolean isSatisfiedBy(Map<String, String> config, StepInput input) {
    Operator operator = ConditionConfig.requireEnum(config, OPERATOR, Operator.class);
    BigDecimal expected = ConditionConfig.requireNumber(config, VALUE);
    return input
        .find(ConditionConfig.require(config, ConditionConfig.KEY))
        .map(
            value ->
                value instanceof NumberValue(BigDecimal actual) && operator.test(actual, expected))
        .orElse(false);
  }
}
