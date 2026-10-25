package ai.coworkers.core.model.runtime.saga;

/** 사가가 흐름을 진행하며 거치는 상태 */
public enum SagaState {
  /** 현재 노드에서 흐름을 진행하는 상태 */
  RUNNING(false),
  /** 병렬 분기 후 갈래가 모두 합류하기를 기다리는 상태 */
  WAITING(false),
  /** 갈래가 합류 슬롯에 도착해 끝난 상태 */
  JOINED(true),
  /** 다음 슬롯 없이 흐름이 끝난 상태 */
  ENDED(true);

  private final boolean finished;

  SagaState(boolean finished) {
    this.finished = finished;
  }

  /**
   * 흐름이 끝난 상태인지 반환함.
   *
   * @return 끝났으면 참
   */
  public boolean isFinished() {
    return finished;
  }
}
