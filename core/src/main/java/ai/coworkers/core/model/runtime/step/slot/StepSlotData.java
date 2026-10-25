package ai.coworkers.core.model.runtime.step.slot;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** 슬롯을 JDK 타입만으로 나타낸 저장 형태. 템플릿은 식별 값과 버전으로만 가리킴. */
public sealed interface StepSlotData {

  /**
   * 슬롯 식별 값을 반환함.
   *
   * @return 식별 값
   */
  UUID id();

  /**
   * 슬롯이 보유한 템플릿의 식별 값을 반환함.
   *
   * @return 템플릿 식별 값
   */
  UUID templateId();

  /**
   * 슬롯이 보유한 템플릿의 버전을 반환함.
   *
   * @return 템플릿 버전
   */
  Long templateVersion();

  /**
   * 작업 슬롯의 저장 형태
   *
   * @param id 식별 값
   * @param templateId 템플릿 식별 값
   * @param templateVersion 템플릿 버전
   * @param next 다음 슬롯 식별 값. 없으면 흐름이 끝남
   */
  record TaskSlotData(UUID id, UUID templateId, Long templateVersion, @Nullable UUID next)
      implements StepSlotData {}

  /**
   * 분기 슬롯의 저장 형태
   *
   * @param id 식별 값
   * @param templateId 템플릿 식별 값
   * @param templateVersion 템플릿 버전
   * @param whenSatisfied 조건을 만족할 때 이어지는 슬롯 식별 값
   * @param otherwise 조건을 만족하지 않을 때 이어지는 슬롯 식별 값
   */
  record BranchSlotData(
      UUID id, UUID templateId, Long templateVersion, UUID whenSatisfied, UUID otherwise)
      implements StepSlotData {}

  /**
   * 반복 슬롯의 저장 형태
   *
   * @param id 식별 값
   * @param templateId 템플릿 식별 값
   * @param templateVersion 템플릿 버전
   * @param body 반복할 때 이어지는 슬롯 식별 값
   * @param exit 반복을 끝낼 때 이어지는 슬롯 식별 값
   */
  record LoopSlotData(UUID id, UUID templateId, Long templateVersion, UUID body, UUID exit)
      implements StepSlotData {}

  /**
   * 병렬 슬롯의 저장 형태
   *
   * @param id 식별 값
   * @param templateId 템플릿 식별 값
   * @param templateVersion 템플릿 버전
   * @param branches 동시에 시작하는 슬롯 식별 값
   * @param join 모든 갈래가 끝난 뒤 이어지는 슬롯 식별 값
   */
  record ParallelSlotData(
      UUID id, UUID templateId, Long templateVersion, List<UUID> branches, UUID join)
      implements StepSlotData {

    public ParallelSlotData {
      branches = List.copyOf(branches);
    }
  }
}
