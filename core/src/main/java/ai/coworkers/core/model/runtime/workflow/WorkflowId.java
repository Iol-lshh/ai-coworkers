package ai.coworkers.core.model.runtime.workflow;

import ai.coworkers.core.common.UuidV7;
import java.util.UUID;

/**
 * 워크플로우 실행을 식별하는 값
 *
 * @param value 식별 값
 */
public record WorkflowId(UUID value) {

  /**
   * 새 식별자를 생성함.
   *
   * @return 생성한 식별자
   */
  public static WorkflowId generate() {
    return new WorkflowId(UuidV7.generate());
  }
}
