package ai.coworkers.core.model.template.step.condition;

import ai.coworkers.core.model.template.step.input.StepInput;
import java.util.List;

/**
 * 자식 조건을 모두 만족해야 하는 조합 노드
 *
 * @param conditions 자식 조건. 하나 이상
 */
record AllOf(List<StepCondition> conditions) implements StepCondition {

  AllOf {
    ChildConditions.REQUIRED.isSatisfiedBy(conditions);
    conditions = List.copyOf(conditions);
  }

  @Override
  public boolean isSatisfiedBy(StepInput candidate) {
    return conditions.stream().allMatch(condition -> condition.isSatisfiedBy(candidate));
  }
}
