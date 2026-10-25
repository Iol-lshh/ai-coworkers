package ai.coworkers.core.model.runtime.step.output;

import ai.coworkers.core.model.runtime.step.slot.StepSlotId;
import java.util.List;

/**
 * 동시에 시작할 갈래와 합류 슬롯을 담은 출력
 *
 * @param branches 동시에 시작하는 슬롯
 * @param join 모든 갈래가 끝난 뒤 이어지는 슬롯
 */
public record ForkOutput(List<StepSlotId> branches, StepSlotId join) implements StepOutput {

  public ForkOutput {
    branches = List.copyOf(branches);
  }
}
