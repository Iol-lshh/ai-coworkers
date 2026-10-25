package ai.coworkers.core.model.runtime.step.slot;

import ai.coworkers.core.model.runtime.step.output.StepOutput;
import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.input.StepInput;

/** 템플릿을 워크플로우의 한 자리에 놓고 출구를 다음 슬롯에 잇는 단위 */
public interface StepSlot {

  /** 슬롯 식별자 */
  StepSlotId id();

  /** 템플릿 */
  StepTemplate template();

  /**
   * 템플릿으로 단계를 처리하고 다음 슬롯을 담은 출력을 반환함.
   *
   * @param input 단계 입력
   * @return 단계 출력
   */
  StepOutput process(StepInput input);
}
