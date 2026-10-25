package ai.coworkers.core.model.runtime.workflow;

import ai.coworkers.core.model.runtime.saga.Saga;
import ai.coworkers.core.model.runtime.saga.SagaCodec;
import ai.coworkers.core.model.runtime.saga.SagaData;
import ai.coworkers.core.model.runtime.saga.SagaId;
import ai.coworkers.core.model.runtime.step.slot.StepSlot;
import ai.coworkers.core.model.runtime.step.slot.StepSlotCodec;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotId;
import ai.coworkers.core.model.runtime.workflow.WorkflowData.SagaJoinData;
import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.workflow.WorkflowTemplateId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

/** 워크플로우 실행을 저장 형태로 바꾸고, 저장 형태와 템플릿 스냅샷으로 실행을 복원하는 진입점 */
public final class WorkflowCodec {

  private final StepSlotCodec slotCodec;
  private final SagaCodec sagaCodec;

  /**
   * 슬롯과 사가 변환에 쓸 코덱을 받음.
   *
   * @param slotCodec 슬롯 코덱
   * @param sagaCodec 사가 코덱
   */
  public WorkflowCodec(StepSlotCodec slotCodec, SagaCodec sagaCodec) {
    this.slotCodec = slotCodec;
    this.sagaCodec = sagaCodec;
  }

  /**
   * 실행을 저장 형태로 바꿈. 저장 형태가 없는 슬롯 종류가 있으면 거부함.
   *
   * @param workflow 워크플로우 실행
   * @return 저장 형태
   */
  public WorkflowData encode(Workflow workflow) {
    return new WorkflowData(
        workflow.id().value(),
        workflow.templateId().value(),
        workflow.templateVersion(),
        workflow.slots().values().stream().map(slotCodec::encode).toList(),
        workflow.sagas().values().stream().map(sagaCodec::encode).toList(),
        workflow.joins().stream().map(WorkflowCodec::encodeJoin).toList());
  }

  /**
   * 저장 형태로 실행을 복원함. 슬롯은 기록된 템플릿 버전의 스냅샷으로 복원함. 없는 슬롯을 가리키거나 슬롯·사가 복원 규칙을 어기면 거부함.
   *
   * @param data 저장 형태
   * @param snapshotOf 템플릿 식별자와 버전으로 템플릿 스냅샷을 찾는 함수
   * @return 워크플로우 실행
   */
  public Workflow decode(
      WorkflowData data, BiFunction<StepTemplateId, Long, StepTemplate> snapshotOf) {
    Map<StepSlotId, StepSlot> slots = new HashMap<>();
    for (StepSlotData slot : data.slots()) {
      StepSlot decoded = slotCodec.decode(slot, snapshotOf);
      slots.put(decoded.id(), decoded);
    }
    Function<StepSlotId, StepSlot> slotOf =
        id -> {
          StepSlot slot = slots.get(id);
          if (slot == null) {
            throw new IllegalArgumentException("unknown slot id: " + id);
          }
          return slot;
        };
    Map<SagaId, Saga> sagas = new HashMap<>();
    for (SagaData saga : data.sagas()) {
      Saga decoded = sagaCodec.decode(saga, slotOf);
      sagas.put(decoded.id(), decoded);
    }
    return new Workflow(
        new WorkflowId(data.id()),
        new WorkflowTemplateId(data.templateId()),
        data.templateVersion(),
        slots,
        sagas,
        data.joins().stream().map(WorkflowCodec::decodeJoin).toList());
  }

  private static SagaJoinData encodeJoin(SagaJoin join) {
    return new SagaJoinData(
        join.origin().value(),
        join.branches().stream().map(SagaId::value).toList(),
        join.join().value(),
        join.joined().stream().map(SagaId::value).toList());
  }

  private static SagaJoin decodeJoin(SagaJoinData join) {
    return new SagaJoin(
        new SagaId(join.origin()),
        sagaIds(join.branches()),
        new StepSlotId(join.join()),
        new HashSet<>(sagaIds(join.joined())));
  }

  private static List<SagaId> sagaIds(List<UUID> values) {
    return values.stream().map(SagaId::new).toList();
  }
}
