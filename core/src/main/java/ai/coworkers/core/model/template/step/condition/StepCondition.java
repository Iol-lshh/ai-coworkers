package ai.coworkers.core.model.template.step.condition;

import ai.coworkers.core.common.Specification;
import ai.coworkers.core.model.template.step.input.StepInput;

/** 분기·반복 템플릿이 보유하는 조건 트리의 노드 */
public sealed interface StepCondition extends Specification<StepInput>
    permits AllOf, AnyOf, Not, Leaf {}
