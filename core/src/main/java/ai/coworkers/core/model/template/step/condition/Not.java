package ai.coworkers.core.model.template.step.condition;

import ai.coworkers.core.model.template.step.input.StepInput;

/**
 * 자식 조건의 판정을 반전하는 조합 노드
 *
 * @param condition 자식 조건
 */
record Not(StepCondition condition) implements StepCondition {

  @Override
  public boolean isSatisfiedBy(StepInput candidate) {
    return !condition.isSatisfiedBy(candidate);
  }
}
