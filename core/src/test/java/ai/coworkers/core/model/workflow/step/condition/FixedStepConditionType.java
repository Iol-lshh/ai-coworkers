package ai.coworkers.core.model.workflow.step.condition;

import ai.coworkers.core.model.workflow.step.input.StepInput;
import java.util.Map;

/**
 * 설정값과 입력에 관계없이 정해진 판정을 반환하는 테스트용 조건 종류
 *
 * @param code 식별 코드
 * @param satisfied 반환할 판정
 */
public record FixedStepConditionType(String code, boolean satisfied) implements StepConditionType {

  /**
   * 정해진 판정을 반환하는 말단 조건을 만듦.
   *
   * @param satisfied 반환할 판정
   * @return 말단 조건
   */
  public static StepCondition leaf(boolean satisfied) {
    return new Leaf(new FixedStepConditionType("test.fixed", satisfied), Map.of());
  }

  @Override
  public void validate(Map<String, String> config) {}

  @Override
  public boolean isSatisfiedBy(Map<String, String> config, StepInput input) {
    return satisfied;
  }
}
