package ai.coworkers.core.model.workflow.step.condition;

import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.input.StepInputValue.BoolValue;
import java.util.Map;

/** 입력의 논리값이 기대값과 같은지 판정하는 조건 종류. 설정값은 {@code key}, {@code expected}. 값이 없거나 논리값이 아니면 만족하지 않음. */
record BoolConditionType() implements StepConditionType {

  private static final String EXPECTED = "expected";

  @Override
  public String code() {
    return "core.bool";
  }

  @Override
  public void validate(Map<String, String> config) {
    ConditionConfig.require(config, ConditionConfig.KEY);
    ConditionConfig.requireBoolean(config, EXPECTED);
  }

  @Override
  public boolean isSatisfiedBy(Map<String, String> config, StepInput input) {
    boolean expected = ConditionConfig.requireBoolean(config, EXPECTED);
    return input
        .find(ConditionConfig.require(config, ConditionConfig.KEY))
        .map(value -> value instanceof BoolValue(boolean actual) && actual == expected)
        .orElse(false);
  }
}
