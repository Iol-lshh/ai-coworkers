package ai.coworkers.core.model.workflow.saga;

import ai.coworkers.core.common.UuidV7;
import java.util.UUID;

/**
 * 사가를 식별하는 값
 *
 * @param value 식별 값
 */
public record SagaId(UUID value) {

  /**
   * 새 식별자를 생성함.
   *
   * @return 생성한 식별자
   */
  public static SagaId generate() {
    return new SagaId(UuidV7.generate());
  }
}
