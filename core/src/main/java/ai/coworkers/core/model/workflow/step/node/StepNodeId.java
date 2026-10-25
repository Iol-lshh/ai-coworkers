package ai.coworkers.core.model.workflow.step.node;

import ai.coworkers.core.common.UuidV7;
import java.util.UUID;

/**
 * 단계 노드를 식별하는 값
 *
 * @param value 식별 값
 */
public record StepNodeId(UUID value) {

  /**
   * 새 식별자를 생성함.
   *
   * @return 생성한 식별자
   */
  public static StepNodeId generate() {
    return new StepNodeId(UuidV7.generate());
  }
}
