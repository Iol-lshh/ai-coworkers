package ai.coworkers.core.model.runtime.step.output;

import ai.coworkers.core.model.runtime.step.slot.StepSlotId;

/**
 * 다음에 실행할 슬롯을 담은 출력
 *
 * @param next 다음 슬롯
 */
public record NextSlotOutput(StepSlotId next) implements StepOutput {}
