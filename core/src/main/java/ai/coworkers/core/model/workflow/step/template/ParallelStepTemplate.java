package ai.coworkers.core.model.workflow.step.template;

import ai.coworkers.core.model.workflow.step.DefaultStepType;
import ai.coworkers.core.model.workflow.step.StepType;
import java.util.List;

/**
 * 여러 갈래를 동시에 시작하고 모두 끝나면 합류하는 단계의 틀
 *
 * @param id 템플릿 식별자
 * @param version 템플릿 버전
 * @param branches 동시에 시작하는 템플릿
 * @param join 모든 갈래가 끝난 뒤 이어지는 템플릿
 */
public record ParallelStepTemplate(
    StepTemplateId id, Long version, List<StepTemplateId> branches, StepTemplateId join)
    implements StepTemplate {

  public ParallelStepTemplate {
    branches = List.copyOf(branches);
  }

  @Override
  public StepType stepType() {
    return DefaultStepType.PARALLEL;
  }

  @Override
  public boolean isTaskStep() {
    return false;
  }
}
