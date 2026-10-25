package ai.coworkers.core.model.runtime.step.node;

import ai.coworkers.core.model.runtime.step.slot.StepSlot;

/**
 * 슬롯을 한 번 실행하는 노드
 *
 * @param id 노드 식별자
 * @param stepSlot 실행하는 슬롯
 * @param nodeState 노드 상태
 */
public record DefaultStepNode(StepNodeId id, StepSlot stepSlot, StepNodeState nodeState)
    implements StepNode {

  /**
   * 슬롯에 도착한 새 노드를 만듦.
   *
   * @param slot 도착한 슬롯
   * @return 도착 상태의 노드
   */
  public static DefaultStepNode arrive(StepSlot slot) {
    return new DefaultStepNode(StepNodeId.generate(), slot, StepNodeState.ARRIVED);
  }
}
