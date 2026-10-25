package ai.coworkers.core.model.runtime.workflow;

import ai.coworkers.core.model.runtime.saga.SagaData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData;
import java.util.List;
import java.util.UUID;

/**
 * 워크플로우 실행을 JDK 타입만으로 나타낸 저장 형태
 *
 * @param id 식별 값
 * @param templateId 워크플로우 틀 식별 값
 * @param templateVersion 처음 실행한 시점의 워크플로우 틀 버전
 * @param slots 슬롯의 저장 형태
 * @param sagas 사가의 저장 형태
 * @param joins 합류 대기의 저장 형태
 */
public record WorkflowData(
    UUID id,
    UUID templateId,
    Long templateVersion,
    List<StepSlotData> slots,
    List<SagaData> sagas,
    List<SagaJoinData> joins) {

  public WorkflowData {
    slots = List.copyOf(slots);
    sagas = List.copyOf(sagas);
    joins = List.copyOf(joins);
  }

  /**
   * 합류 대기의 저장 형태
   *
   * @param origin 분기하고 합류를 기다리는 사가 식별 값
   * @param branches 갈래 사가 식별 값
   * @param join 모든 갈래가 도착하면 이어지는 슬롯 식별 값
   * @param joined 합류 슬롯에 도착한 갈래 사가 식별 값
   */
  public record SagaJoinData(UUID origin, List<UUID> branches, UUID join, List<UUID> joined) {

    public SagaJoinData {
      branches = List.copyOf(branches);
      joined = List.copyOf(joined);
    }
  }
}
