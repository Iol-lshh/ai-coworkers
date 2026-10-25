package ai.coworkers.core.model.workflow.step.template.task;

import ai.coworkers.core.model.workflow.step.StepType;

/** 작업 단계가 수행하는 작업 종류 */
public interface TaskType extends StepType {

  /**
   * 저장·조회에 쓰는 식별 코드를 반환함. 모듈 접두어를 포함하며 모든 작업 종류 사이에서 유일함.
   *
   * @return 식별 코드
   */
  String code();
}
