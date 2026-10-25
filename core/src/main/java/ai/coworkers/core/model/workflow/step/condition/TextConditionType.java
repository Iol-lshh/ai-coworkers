package ai.coworkers.core.model.workflow.step.condition;

import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.input.StepInputValue.TextValue;
import java.util.Map;

/**
 * 입력의 문자열값이 기준값과 같은지 판정하는 조건 종류. 설정값은 {@code key}, {@code value}. 앞뒤 공백을 제거하고 비교함. 값이 없거나 문자열값이 아니면
 * 만족하지 않음.
 */
record TextConditionType() implements StepConditionType {

  private static final String VALUE = "value";

  @Override
  public String code() {
    return "core.text";
  }

  @Override
  public void validate(Map<String, String> config) {
    ConditionConfig.require(config, ConditionConfig.KEY);
    ConditionConfig.require(config, VALUE);
  }

  @Override
  public boolean isSatisfiedBy(Map<String, String> config, StepInput input) {
    String expected = ConditionConfig.require(config, VALUE).strip();
    return input
        .find(ConditionConfig.require(config, ConditionConfig.KEY))
        .map(value -> value instanceof TextValue(String actual) && actual.strip().equals(expected))
        .orElse(false);
  }
}
