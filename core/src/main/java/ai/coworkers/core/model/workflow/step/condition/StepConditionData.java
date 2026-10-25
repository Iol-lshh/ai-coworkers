package ai.coworkers.core.model.workflow.step.condition;

import java.util.List;
import java.util.Map;

/** 조건 트리 노드를 JDK 타입만으로 나타낸 저장 형태 */
public sealed interface StepConditionData {

  /**
   * 전부 만족 노드의 저장 형태
   *
   * @param children 자식 노드
   */
  record AllOfData(List<StepConditionData> children) implements StepConditionData {

    public AllOfData {
      children = List.copyOf(children);
    }
  }

  /**
   * 하나 이상 만족 노드의 저장 형태
   *
   * @param children 자식 노드
   */
  record AnyOfData(List<StepConditionData> children) implements StepConditionData {

    public AnyOfData {
      children = List.copyOf(children);
    }
  }

  /**
   * 부정 노드의 저장 형태
   *
   * @param child 자식 노드
   */
  record NotData(StepConditionData child) implements StepConditionData {}

  /**
   * 말단 노드의 저장 형태
   *
   * @param code 조건 종류 코드
   * @param config 설정값
   */
  record LeafData(String code, Map<String, String> config) implements StepConditionData {

    public LeafData {
      config = Map.copyOf(config);
    }
  }
}
