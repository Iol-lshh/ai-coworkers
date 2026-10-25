package ai.coworkers.core.model.workflow.step.template.task;

import ai.coworkers.core.model.workflow.step.DefaultStepType;
import ai.coworkers.core.model.workflow.step.StepType;
import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.output.StepOutput;
import ai.coworkers.core.model.workflow.step.template.StepTemplate;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import org.jspecify.annotations.Nullable;

/** 작업 종류에 맞는 작업을 실행하는 단계의 틀 */
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
   * 작업을 실행함.
   *
   * @param input 단계 입력
   * @return 작업 결과
   */
  StepOutput process(StepInput input);

  /**
   * 작업이 끝난 뒤 이어지는 템플릿을 반환함.
   *
   * @return 다음 템플릿. 없으면 흐름이 끝남
   */
  @Nullable StepTemplateId next();
}
