package ai.coworkers.core.model.workflow.step.template;

import ai.coworkers.core.model.workflow.step.DefaultStepType;
import ai.coworkers.core.model.workflow.step.StepType;
import ai.coworkers.core.model.workflow.step.condition.StepCondition;

/**
 * 조건을 만족하는 동안 본문을 반복하는 단계의 틀
 *
 * @param id 템플릿 식별자
 * @param version 템플릿 버전
 * @param condition 반복 조건
 * @param body 반복할 때 이어지는 템플릿
 * @param exit 반복을 끝낼 때 이어지는 템플릿
 */
public record LoopStepTemplate(
    StepTemplateId id,
    Long version,
    StepCondition condition,
    StepTemplateId body,
    StepTemplateId exit)
    implements StepTemplate {
  @Override
  public StepType stepType() {
    return DefaultStepType.LOOP;
  }

  @Override
  public boolean isTaskStep() {
    return false;
  }
}
