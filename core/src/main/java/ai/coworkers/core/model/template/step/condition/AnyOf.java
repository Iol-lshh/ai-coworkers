package ai.coworkers.core.model.template.step.condition;

import ai.coworkers.core.model.template.step.input.StepInput;
import java.util.List;

/**
 * 자식 조건을 하나 이상 만족하면 되는 조합 노드
 *
 * @param conditions 자식 조건. 하나 이상
 */
record AnyOf(List<StepCondition> conditions) implements StepCondition {

  AnyOf {
    ChildConditions.REQUIRED.isSatisfiedBy(conditions);
    conditions = List.copyOf(conditions);
  }

  @Override
  public boolean isSatisfiedBy(StepInput candidate) {
    return conditions.stream().anyMatch(condition -> condition.isSatisfiedBy(candidate));
  }
}
