package ai.coworkers.core.model.runtime.saga;

import ai.coworkers.core.model.runtime.step.node.DefaultStepNode;
import ai.coworkers.core.model.runtime.step.node.StepNode;
import ai.coworkers.core.model.runtime.step.slot.StepSlot;
import org.jspecify.annotations.Nullable;

/**
 * 흐름 하나가 지금 가리키는 노드
 *
 * @param id 사가 식별자
 * @param state 사가 상태
 * @param activeNode 현재 노드. 흐름이 끝났으면 없음
 */
public record Saga(SagaId id, SagaState state, @Nullable StepNode activeNode) {

  /** 끝나지 않은 상태에 현재 노드가 없거나, 끝난 상태에 현재 노드가 있으면 거부함. */
  public Saga {
    if (state.isFinished() == (activeNode != null)) {
      throw new IllegalArgumentException("saga state mismatch with active node: " + state);
    }
  }

  /**
   * 슬롯에서 흐름을 시작하는 새 사가를 만듦.
   *
   * @param slot 시작 슬롯
   * @return 진행 상태의 사가
   */
  public static Saga start(StepSlot slot) {
    return new Saga(SagaId.generate(), SagaState.RUNNING, DefaultStepNode.arrive(slot));
  }

  /**
   * 다음 슬롯의 새 노드로 옮김. 진행 상태가 아니면 거부함.
   *
   * @param slot 다음 슬롯
   * @return 옮긴 사가
   */
  public Saga moveTo(StepSlot slot) {
    requireState(SagaState.RUNNING);
    return new Saga(id, SagaState.RUNNING, DefaultStepNode.arrive(slot));
  }

  /**
   * 현재 노드에서 합류를 기다림. 진행 상태가 아니면 거부함.
   *
   * @return 대기 상태의 사가
   */
  public Saga await() {
    requireState(SagaState.RUNNING);
    return new Saga(id, SagaState.WAITING, activeNode);
  }

  /**
   * 합류 대기를 마치고 현재 노드에서 다시 진행함. 대기 상태가 아니면 거부함.
   *
   * @return 진행 상태의 사가
   */
  public Saga resume() {
    requireState(SagaState.WAITING);
    return new Saga(id, SagaState.RUNNING, activeNode);
  }

  /**
   * 합류 슬롯에 도착해 끝냄. 진행 상태가 아니면 거부함.
   *
   * @return 합류 상태의 사가
   */
  public Saga join() {
    requireState(SagaState.RUNNING);
    return new Saga(id, SagaState.JOINED, null);
  }

  /**
   * 다음 슬롯 없이 끝냄. 진행 상태가 아니면 거부함.
   *
   * @return 종료 상태의 사가
   */
  public Saga end() {
    requireState(SagaState.RUNNING);
    return new Saga(id, SagaState.ENDED, null);
  }

  private void requireState(SagaState expected) {
    if (state != expected) {
      throw new IllegalStateException("saga " + id + " is " + state + ", expected " + expected);
    }
  }
}
