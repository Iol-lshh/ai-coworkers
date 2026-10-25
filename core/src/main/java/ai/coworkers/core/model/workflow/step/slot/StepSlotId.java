package ai.coworkers.core.model.workflow.step.slot;

import ai.coworkers.core.common.UuidV7;
import java.util.UUID;

/**
 * 워크플로우 안에서 슬롯을 식별하는 값
 *
 * @param value 식별 값
 */
public record StepSlotId(UUID value) {

  /**
   * 새 식별자를 생성함.
   *
   * @return 생성한 식별자
   */
  public static StepSlotId generate() {
    return new StepSlotId(UuidV7.generate());
  }
}
