package ai.coworkers.core.model.template.step.condition;

import ai.coworkers.core.model.template.step.input.StepInput;
import java.util.Map;

/**
 * 조건 종류에 판정을 위임하는 말단 노드
 *
 * @param type 조건 종류
 * @param config 조건 종류에 넘길 설정값
 */
record Leaf(StepConditionType type, Map<String, String> config) implements StepCondition {

  Leaf {
    type.validate(config);
    config = Map.copyOf(config);
  }

  @Override
  public boolean isSatisfiedBy(StepInput candidate) {
    return type.isSatisfiedBy(config, candidate);
  }
}
