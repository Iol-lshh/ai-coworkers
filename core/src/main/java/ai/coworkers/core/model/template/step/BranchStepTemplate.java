package ai.coworkers.core.model.template.step;

import ai.coworkers.core.model.template.step.condition.StepCondition;

/**
 * 조건 만족 여부로 다음 단계를 고르는 단계의 틀
 *
 * @param id 템플릿 식별자
 * @param version 템플릿 버전
 * @param condition 판정 조건
 * @param whenSatisfied 조건을 만족할 때 이어지는 템플릿
 * @param otherwise 조건을 만족하지 않을 때 이어지는 템플릿
 */
public record BranchStepTemplate(
    StepTemplateId id,
    Long version,
    StepCondition condition,
    StepTemplateId whenSatisfied,
    StepTemplateId otherwise)
    implements StepTemplate {
  @Override
  public StepType stepType() {
    return DefaultStepType.BRANCH;
  }

  @Override
  public boolean isTaskStep() {
    return false;
  }
}
