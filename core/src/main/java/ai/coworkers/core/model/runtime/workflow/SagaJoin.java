package ai.coworkers.core.model.runtime.workflow;

import ai.coworkers.core.model.runtime.saga.SagaId;
import ai.coworkers.core.model.runtime.step.slot.StepSlotId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 병렬 분기 하나의 합류 대기
 *
 * @param origin 분기하고 합류를 기다리는 사가
 * @param branches 갈래 사가
 * @param join 모든 갈래가 도착하면 이어지는 슬롯
 * @param joined 합류 슬롯에 도착한 갈래 사가
 */
public record SagaJoin(SagaId origin, List<SagaId> branches, StepSlotId join, Set<SagaId> joined) {

  /** 갈래가 아닌 사가가 도착해 있으면 거부함. */
  public SagaJoin {
    branches = List.copyOf(branches);
    joined = Set.copyOf(joined);
    if (!branches.containsAll(joined)) {
      throw new IllegalArgumentException("joined saga not in branches: " + joined);
    }
  }

  /**
   * 사가가 이 분기의 갈래인지 반환함.
   *
   * @param saga 사가 식별자
   * @return 갈래이면 참
   */
  public boolean hasBranch(SagaId saga) {
    return branches.contains(saga);
  }

  /**
   * 갈래의 도착을 기록함.
   *
   * @param branch 도착한 갈래 사가
   * @return 도착을 기록한 합류 대기
   */
  public SagaJoin arrive(SagaId branch) {
    Set<SagaId> next = new HashSet<>(joined);
    next.add(branch);
    return new SagaJoin(origin, branches, join, next);
  }

  /**
   * 갈래가 모두 도착했는지 반환함.
   *
   * @return 모두 도착했으면 참
   */
  public boolean isComplete() {
    return joined.containsAll(branches);
  }
}
