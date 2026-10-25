package ai.coworkers.core.model.runtime.step.slot.task;

import ai.coworkers.core.model.runtime.step.output.StepOutput;
import ai.coworkers.core.model.template.step.input.StepInput;
import ai.coworkers.core.model.template.step.task.TaskStepTemplate;

/** 작업 템플릿이 정의한 작업을 실행하는 실행기 */
@FunctionalInterface
public interface TaskExecutor {

  /**
   * 작업을 실행함.
   *
   * @param template 작업 템플릿
   * @param input 단계 입력
   * @return 작업 결과
   */
  StepOutput execute(TaskStepTemplate template, StepInput input);
}
