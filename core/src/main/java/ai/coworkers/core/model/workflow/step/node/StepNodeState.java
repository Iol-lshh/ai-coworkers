package ai.coworkers.core.model.workflow.step.node;

import ai.coworkers.core.model.workflow.step.slot.StepSlotState;

/** 노드가 슬롯을 한 번 실행하며 거치는 상태 */
public enum StepNodeState {
  /** 슬롯에 도착했고 입력이 정해지지 않은 상태 */
  ARRIVED(StepSlotState.PENDING),
  /** 입력이 정해진 상태 */
  READY(StepSlotState.PENDING),
  /** 슬롯을 처리하는 중인 상태 */
  RUNNING(StepSlotState.RUNNING),
  /** 처리에 성공한 상태 */
  SUCCEEDED(StepSlotState.RESOLVED),
  /** 재시도를 모두 소진하고 실패한 상태 */
  FAILED(StepSlotState.RESOLVED),
  /** 끝나기 전에 중단된 상태 */
  CANCELLED(StepSlotState.RESOLVED);

  private final StepSlotState slotState;

  StepNodeState(StepSlotState slotState) {
    this.slotState = slotState;
  }

  /**
   * 이 상태에 대응하는 슬롯 상태를 반환함.
   *
   * @return 슬롯 상태
   */
  public StepSlotState slotState() {
    return slotState;
  }
}
