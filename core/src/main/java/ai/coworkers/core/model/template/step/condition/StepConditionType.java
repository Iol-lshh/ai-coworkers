package ai.coworkers.core.model.template.step.condition;

import ai.coworkers.core.model.template.step.input.StepInput;
import java.util.Map;

/** 말단 조건의 판정 방식을 정하는 조건 종류 */
public interface StepConditionType {

  /**
   * 저장·조회에 쓰는 식별 코드를 반환함. 모듈 접두어를 포함하며 모든 조건 종류 사이에서 유일함.
   *
   * @return 식별 코드
   */
  String code();

  /**
   * 설정값이 판정에 쓸 수 있는 형태인지 검증함. 아니면 거부함.
   *
   * @param config 말단 조건의 설정값
   */
  void validate(Map<String, String> config);

  /**
   * 설정값을 기준으로 입력이 조건을 만족하는지 판정함. 입력에만 의존해 결정적으로 판정함.
   *
   * @param config 검증을 통과한 말단 조건의 설정값
   * @param input 단계 입력
   * @return 조건을 만족하면 {@code true}
   */
  boolean isSatisfiedBy(Map<String, String> config, StepInput input);
}
