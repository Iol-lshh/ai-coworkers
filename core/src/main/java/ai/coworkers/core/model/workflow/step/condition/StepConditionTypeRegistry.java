package ai.coworkers.core.model.workflow.step.condition;

import static ai.coworkers.core.common.ArgumentSpecifications.NOT_BLANK;

import ai.coworkers.core.common.Specification;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** 식별 코드로 조건 종류를 찾는 등록부 */
final class StepConditionTypeRegistry {

  private static final Specification<String> CODE =
      NOT_BLANK.orElseThrow(
          value -> new IllegalArgumentException("condition type code must not be blank"));

  private final Map<String, StepConditionType> conditionTypes;

  /**
   * 조건 종류를 등록함. 코드가 중복되면 거부함.
   *
   * @param conditionTypes 등록할 조건 종류
   */
  StepConditionTypeRegistry(Collection<? extends StepConditionType> conditionTypes) {
    Map<String, StepConditionType> registered = new HashMap<>();
    for (StepConditionType conditionType : conditionTypes) {
      String code = conditionType.code();
      CODE.isSatisfiedBy(code);
      if (registered.putIfAbsent(code, conditionType) != null) {
        throw new IllegalArgumentException("duplicate condition type code: " + code);
      }
    }
    this.conditionTypes = Map.copyOf(registered);
  }

  /**
   * 코드에 해당하는 조건 종류를 반환함. 등록되지 않은 코드면 거부함.
   *
   * @param code 식별 코드
   * @return 조건 종류
   */
  StepConditionType find(String code) {
    StepConditionType conditionType = conditionTypes.get(code);
    if (conditionType == null) {
      throw new IllegalArgumentException("unknown condition type code: " + code);
    }
    return conditionType;
  }
}
