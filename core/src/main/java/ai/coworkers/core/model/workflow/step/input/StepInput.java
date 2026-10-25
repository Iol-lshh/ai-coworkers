package ai.coworkers.core.model.workflow.step.input;

import java.util.Map;
import java.util.Optional;

/**
 * 단계에 넘기는 이름 붙은 값 묶음
 *
 * @param values 이름별 값
 */
public record StepInput(Map<String, StepInputValue> values) {

  private static final StepInput EMPTY = new StepInput(Map.of());

  public StepInput {
    values = Map.copyOf(values);
  }

  /**
   * 값이 없는 입력을 반환함.
   *
   * @return 빈 입력
   */
  public static StepInput empty() {
    return EMPTY;
  }

  /**
   * 이름에 해당하는 값을 찾음.
   *
   * @param key 값 이름
   * @return 값. 없으면 빈 값
   */
  public Optional<StepInputValue> find(String key) {
    return Optional.ofNullable(values.get(key));
  }
}
