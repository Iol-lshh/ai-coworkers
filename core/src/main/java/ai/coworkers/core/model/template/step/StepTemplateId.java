package ai.coworkers.core.model.template.step;

import ai.coworkers.core.common.UuidV7;
import java.util.UUID;

/**
 * 템플릿을 식별하는 값
 *
 * @param value 식별 값
 */
public record StepTemplateId(UUID value) {

  /**
   * 새 식별자를 생성함.
   *
   * @return 생성한 식별자
   */
  public static StepTemplateId generate() {
    return new StepTemplateId(UuidV7.generate());
  }
}
