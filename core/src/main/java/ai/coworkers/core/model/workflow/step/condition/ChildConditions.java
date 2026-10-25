package ai.coworkers.core.model.workflow.step.condition;

import static ai.coworkers.core.common.ArgumentSpecifications.NOT_EMPTY;

import ai.coworkers.core.common.Specification;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 조합 노드의 자식 조건 규칙 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ChildConditions {

  /** 자식 조건이 하나 이상임. 아니면 거부함. */
  static final Specification<Collection<?>> REQUIRED =
      NOT_EMPTY.orElseThrow(
          conditions -> new IllegalArgumentException("conditions must not be empty"));
}
