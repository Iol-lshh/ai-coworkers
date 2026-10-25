package ai.coworkers.core.model.runtime.workflow;

import ai.coworkers.core.model.runtime.saga.Saga;
import ai.coworkers.core.model.runtime.saga.SagaId;
import ai.coworkers.core.model.runtime.step.output.ForkOutput;
import ai.coworkers.core.model.runtime.step.output.NextSlotOutput;
import ai.coworkers.core.model.runtime.step.output.StepOutput;
import ai.coworkers.core.model.runtime.step.slot.StepSlot;
import ai.coworkers.core.model.runtime.step.slot.StepSlotId;
import ai.coworkers.core.model.runtime.step.slot.task.TaskSlotOutput;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.workflow.WorkflowTemplate;
import ai.coworkers.core.model.template.workflow.WorkflowTemplateId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * 워크플로우 틀 하나를 찍어낸 실행. 사가의 분기·합류를 관리함.
 *
 * @param id 워크플로우 식별자
 * @param templateId 찍어낸 워크플로우 틀 식별자
 * @param templateVersion 처음 실행한 시점의 워크플로우 틀 버전
 * @param slots 슬롯 식별자별 슬롯
 * @param sagas 사가 식별자별 사가
 * @param joins 합류를 기다리는 병렬 분기
 */
public record Workflow(
    WorkflowId id,
    WorkflowTemplateId templateId,
    Long templateVersion,
    Map<StepSlotId, StepSlot> slots,
    Map<SagaId, Saga> sagas,
    List<SagaJoin> joins) {

  public Workflow {
    slots = Map.copyOf(slots);
    sagas = Map.copyOf(sagas);
    joins = List.copyOf(joins);
  }

  /**
   * 틀과 찍어낸 슬롯으로 실행을 시작함. 시작 템플릿의 슬롯에서 첫 사가를 만듦. 시작 템플릿의 슬롯이 없으면 거부함.
   *
   * @param template 처음 실행하는 시점의 워크플로우 틀
   * @param stamped 틀의 템플릿을 찍어낸 템플릿 식별자별 슬롯
   * @return 시작한 실행
   */
  public static Workflow start(WorkflowTemplate template, Map<StepTemplateId, StepSlot> stamped) {
    StepSlot start = stamped.get(template.start());
    if (start == null) {
      throw new IllegalArgumentException("start slot not stamped: " + template.start());
    }
    Map<StepSlotId, StepSlot> slots = new HashMap<>();
    for (StepSlot slot : stamped.values()) {
      slots.put(slot.id(), slot);
    }
    Saga first = Saga.start(start);
    return new Workflow(
        WorkflowId.generate(),
        template.id(),
        template.version(),
        slots,
        Map.of(first.id(), first),
        List.of());
  }

  /**
   * 사가의 현재 노드가 낸 출력으로 흐름을 진행함.
   *
   * <ul>
   *   <li>다음 슬롯 출력이면 다음 슬롯으로 옮김.
   *   <li>작업 출력이면 다음 슬롯으로 옮기고, 다음 슬롯이 없으면 사가를 끝냄.
   *   <li>병렬 출력이면 사가를 대기시키고 갈래마다 사가를 만듦.
   *   <li>갈래 사가가 자기 분기의 합류 슬롯으로 가면 도착으로 끝냄. 갈래가 모두 도착하면 대기한 사가가 합류 슬롯으로 옮겨감.
   * </ul>
   *
   * 사가가 없거나, 지원하지 않는 출력이거나, 갈래 사가가 합류 전에 끝나면 거부함.
   *
   * @param sagaId 출력을 낸 사가
   * @param output 현재 노드의 출력
   * @return 진행한 실행
   */
  public Workflow advance(SagaId sagaId, StepOutput output) {
    Saga saga = sagas.get(sagaId);
    if (saga == null) {
      throw new NoSuchElementException("unknown saga id: " + sagaId);
    }
    Progress progress = new Progress();
    switch (output) {
      case NextSlotOutput(StepSlotId next) -> progress.moveTo(saga, next);
      case TaskSlotOutput task -> {
        StepSlotId next = task.next();
        if (next == null) {
          progress.end(saga);
        } else {
          progress.moveTo(saga, next);
        }
      }
      case ForkOutput(List<StepSlotId> branches, StepSlotId join) ->
          progress.fork(saga, branches, join);
      default ->
          throw new IllegalArgumentException(
              "unsupported step output: " + output.getClass().getName());
    }
    return new Workflow(id, templateId, templateVersion, slots, progress.sagas, progress.joins);
  }

  /**
   * 모든 사가가 끝났는지 반환함.
   *
   * @return 모두 끝났으면 참
   */
  public boolean isFinished() {
    return sagas.values().stream().allMatch(saga -> saga.state().isFinished());
  }

  /** 진행 한 번 동안 바뀌는 사가와 합류 대기 */
  private final class Progress {

    private final Map<SagaId, Saga> sagas = new HashMap<>(Workflow.this.sagas);
    private final List<SagaJoin> joins = new ArrayList<>(Workflow.this.joins);

    private void moveTo(Saga saga, StepSlotId next) {
      Optional<SagaJoin> join = joinOf(saga.id());
      if (join.isPresent() && join.get().join().equals(next)) {
        arrive(saga, join.get());
        return;
      }
      put(saga.moveTo(slotOf(next)));
    }

    private void end(Saga saga) {
      if (joinOf(saga.id()).isPresent()) {
        throw new IllegalStateException("branch saga ended before join: " + saga.id());
      }
      put(saga.end());
    }

    private void fork(Saga saga, List<StepSlotId> branches, StepSlotId join) {
      put(saga.await());
      List<Saga> started = branches.stream().map(branch -> Saga.start(slotOf(branch))).toList();
      joins.add(new SagaJoin(saga.id(), started.stream().map(Saga::id).toList(), join, Set.of()));
      started.forEach(this::put);
      for (int i = 0; i < started.size(); i++) {
        if (branches.get(i).equals(join)) {
          arrive(started.get(i), joinOf(started.get(i).id()).orElseThrow());
        }
      }
      completeIfReady(saga.id());
    }

    private void arrive(Saga branch, SagaJoin join) {
      put(branch.join());
      joins.set(joins.indexOf(join), join.arrive(branch.id()));
      completeIfReady(join.origin());
    }

    /** 대기한 사가의 갈래가 모두 도착했으면 합류 대기를 지우고 대기한 사가를 합류 슬롯으로 옮김. */
    private void completeIfReady(SagaId origin) {
      Optional<SagaJoin> join =
          joins.stream().filter(candidate -> candidate.origin().equals(origin)).findFirst();
      if (join.isEmpty() || !join.get().isComplete()) {
        return;
      }
      joins.remove(join.get());
      Saga waiting = sagas.get(origin);
      if (waiting == null) {
        throw new IllegalStateException("unknown origin saga id: " + origin);
      }
      Saga resumed = waiting.resume();
      put(resumed);
      moveTo(resumed, join.get().join());
    }

    private Optional<SagaJoin> joinOf(SagaId saga) {
      return joins.stream().filter(join -> join.hasBranch(saga)).findFirst();
    }

    private StepSlot slotOf(StepSlotId id) {
      StepSlot slot = slots.get(id);
      if (slot == null) {
        throw new IllegalArgumentException("unknown slot id: " + id);
      }
      return slot;
    }

    private void put(Saga saga) {
      sagas.put(saga.id(), saga);
    }
  }
}
