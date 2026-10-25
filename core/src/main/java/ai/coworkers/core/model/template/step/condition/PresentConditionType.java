package ai.coworkers.core.model.template.step.condition;

import ai.coworkers.core.model.template.step.input.StepInput;
import java.util.Map;

/** 입력에 값이 있는지 판정하는 조건 종류. 설정값은 {@code key}. */
record PresentConditionType() implements StepConditionType {

  @Override
  public String code() {
    return "core.present";
  }

  @Override
  public void validate(Map<String, String> config) {
    ConditionConfig.require(config, ConditionConfig.KEY);
  }

  @Override
  public boolean isSatisfiedBy(Map<String, String> config, StepInput input) {
    return input.find(ConditionConfig.require(config, ConditionConfig.KEY)).isPresent();
  }
}
