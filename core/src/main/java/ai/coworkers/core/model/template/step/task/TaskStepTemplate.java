package ai.coworkers.core.model.template.step.task;

import ai.coworkers.core.model.template.step.DefaultStepType;
import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.StepType;
import org.jspecify.annotations.Nullable;

/** 작업 단계의 틀. 작업 실행은 작업 슬롯이 맡음. */
public interface TaskStepTemplate extends StepTemplate {
  @Override
  default StepType stepType() {
    return DefaultStepType.TASK;
  }

  @Override
  default boolean isTaskStep() {
    return true;
  }

  /**
   * 작업이 끝난 뒤 이어지는 템플릿을 반환함.
   *
   * @return 다음 템플릿. 없으면 흐름이 끝남
   */
  @Nullable StepTemplateId next();
}
