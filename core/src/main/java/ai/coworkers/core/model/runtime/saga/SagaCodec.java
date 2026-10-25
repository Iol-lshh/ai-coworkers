package ai.coworkers.core.model.runtime.saga;

import ai.coworkers.core.model.runtime.saga.SagaData.NodeData;
import ai.coworkers.core.model.runtime.step.node.DefaultStepNode;
import ai.coworkers.core.model.runtime.step.node.StepNode;
import ai.coworkers.core.model.runtime.step.node.StepNodeId;
import ai.coworkers.core.model.runtime.step.node.StepNodeState;
import ai.coworkers.core.model.runtime.step.slot.StepSlot;
import ai.coworkers.core.model.runtime.step.slot.StepSlotId;
import java.util.function.Function;

/** 사가를 저장 형태로 바꾸고, 저장 형태와 슬롯으로 사가를 복원하는 진입점 */
public final class SagaCodec {

  /**
   * 사가를 저장 형태로 바꿈.
   *
   * @param saga 사가
   * @return 저장 형태
   */
  public SagaData encode(Saga saga) {
    StepNode node = saga.activeNode();
    return new SagaData(
        saga.id().value(),
        saga.state().name(),
        node == null
            ? null
            : new NodeData(
                node.id().value(), node.nodeState().name(), node.stepSlot().id().value()));
  }

  /**
   * 저장 형태로 사가를 복원함. 상태 이름을 모르거나 사가 규칙을 어기면 거부함.
   *
   * @param data 저장 형태
   * @param slotOf 슬롯 식별자로 슬롯을 찾는 함수
   * @return 사가
   */
  public Saga decode(SagaData data, Function<StepSlotId, StepSlot> slotOf) {
    NodeData node = data.node();
    return new Saga(
        new SagaId(data.id()),
        stateOf(SagaState.class, data.state()),
        node == null ? null : nodeOf(node, slotOf));
  }

  private static StepNode nodeOf(NodeData node, Function<StepSlotId, StepSlot> slotOf) {
    return new DefaultStepNode(
        new StepNodeId(node.id()),
        slotOf.apply(new StepSlotId(node.slotId())),
        stateOf(StepNodeState.class, node.state()));
  }

  private static <E extends Enum<E>> E stateOf(Class<E> type, String name) {
    try {
      return Enum.valueOf(type, name);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("unknown " + type.getSimpleName() + ": " + name, e);
    }
  }
}
