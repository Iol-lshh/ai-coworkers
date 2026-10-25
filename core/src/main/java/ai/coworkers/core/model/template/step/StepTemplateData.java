package ai.coworkers.core.model.template.step;

import ai.coworkers.core.model.template.step.condition.StepConditionData;
import java.util.List;
import java.util.UUID;

/** 템플릿을 JDK 타입만으로 나타낸 저장 형태 */
public sealed interface StepTemplateData {

  /**
   * 템플릿 식별 값을 반환함.
   *
   * @return 식별 값
   */
  UUID id();

  /**
   * 템플릿 버전을 반환함.
   *
   * @return 버전
   */
  Long version();

  /**
   * 분기 템플릿의 저장 형태
   *
   * @param id 식별 값
   * @param version 버전
   * @param condition 판정 조건의 저장 형태
   * @param whenSatisfied 조건을 만족할 때 이어지는 템플릿 식별 값
   * @param otherwise 조건을 만족하지 않을 때 이어지는 템플릿 식별 값
   */
  record BranchData(
      UUID id, Long version, StepConditionData condition, UUID whenSatisfied, UUID otherwise)
      implements StepTemplateData {}

  /**
   * 반복 템플릿의 저장 형태
   *
   * @param id 식별 값
   * @param version 버전
   * @param condition 반복 조건의 저장 형태
   * @param body 반복할 때 이어지는 템플릿 식별 값
   * @param exit 반복을 끝낼 때 이어지는 템플릿 식별 값
   */
  record LoopData(UUID id, Long version, StepConditionData condition, UUID body, UUID exit)
      implements StepTemplateData {}

  /**
   * 병렬 템플릿의 저장 형태
   *
   * @param id 식별 값
   * @param version 버전
   * @param branches 동시에 시작하는 템플릿 식별 값
   * @param join 모든 갈래가 끝난 뒤 이어지는 템플릿 식별 값
   */
  record ParallelData(UUID id, Long version, List<UUID> branches, UUID join)
      implements StepTemplateData {

    public ParallelData {
      branches = List.copyOf(branches);
    }
  }
}
