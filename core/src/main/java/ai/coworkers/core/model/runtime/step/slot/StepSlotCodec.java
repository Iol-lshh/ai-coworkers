package ai.coworkers.core.model.runtime.step.slot;

import ai.coworkers.core.model.runtime.step.slot.StepSlotData.BranchSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.LoopSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.ParallelSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.TaskSlotData;
import ai.coworkers.core.model.runtime.step.slot.branch.BranchStepSlot;
import ai.coworkers.core.model.runtime.step.slot.loop.LoopStepSlot;
import ai.coworkers.core.model.runtime.step.slot.parallel.ParallelStepSlot;
import ai.coworkers.core.model.runtime.step.slot.task.TaskStepSlot;
import ai.coworkers.core.model.template.step.BranchStepTemplate;
import ai.coworkers.core.model.template.step.LoopStepTemplate;
import ai.coworkers.core.model.template.step.ParallelStepTemplate;
import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import java.util.UUID;
import java.util.function.BiFunction;
import org.jspecify.annotations.Nullable;

/** 슬롯을 저장 형태로 바꾸고, 저장 형태와 템플릿 스냅샷으로 슬롯을 복원하는 진입점. 작업 슬롯은 작업 템플릿의 저장 형태가 없어 복원을 거부함. */
public final class StepSlotCodec {

  /**
   * 슬롯을 저장 형태로 바꿈. 저장 형태가 없는 슬롯 종류면 거부함.
   *
   * @param slot 슬롯
   * @return 저장 형태
   */
  public StepSlotData encode(StepSlot slot) {
    UUID id = slot.id().value();
    UUID templateId = slot.template().id().value();
    Long templateVersion = slot.template().version();
    return switch (slot) {
      case TaskStepSlot task ->
          new TaskSlotData(id, templateId, templateVersion, valueOf(task.next()));
      case BranchStepSlot branch ->
          new BranchSlotData(
              id,
              templateId,
              templateVersion,
              branch.whenSatisfied().value(),
              branch.otherwise().value());
      case LoopStepSlot loop ->
          new LoopSlotData(
              id, templateId, templateVersion, loop.body().value(), loop.exit().value());
      case ParallelStepSlot parallel ->
          new ParallelSlotData(
              id,
              templateId,
              templateVersion,
              parallel.branches().stream().map(StepSlotId::value).toList(),
              parallel.join().value());
      default ->
          throw new IllegalArgumentException("unsupported slot type: " + slot.getClass().getName());
    };
  }

  /**
   * 저장 형태의 출구를 빌더에 그대로 넣어 슬롯을 복원함. 작업 슬롯이거나 스냅샷의 종류가 슬롯과 맞지 않으면 거부함.
   *
   * @param data 저장 형태
   * @param snapshotOf 템플릿 식별자와 버전으로 템플릿 스냅샷을 찾는 함수
   * @return 슬롯
   */
  public StepSlot decode(
      StepSlotData data, BiFunction<StepTemplateId, Long, StepTemplate> snapshotOf) {
    StepSlotId id = new StepSlotId(data.id());
    return switch (data) {
      case TaskSlotData task ->
          throw new IllegalArgumentException("task slot restore unsupported: " + task.id());
      case BranchSlotData branch -> {
        BranchStepSlot.Builder builder =
            BranchStepSlot.Builder.from(templateOf(data, BranchStepTemplate.class, snapshotOf));
        builder.setId(id);
        builder.setWhenSatisfied(new StepSlotId(branch.whenSatisfied()));
        builder.setOtherwise(new StepSlotId(branch.otherwise()));
        yield builder.toBuild();
      }
      case LoopSlotData loop -> {
        LoopStepSlot.Builder builder =
            LoopStepSlot.Builder.from(templateOf(data, LoopStepTemplate.class, snapshotOf));
        builder.setId(id);
        builder.setBody(new StepSlotId(loop.body()));
        builder.setExit(new StepSlotId(loop.exit()));
        yield builder.toBuild();
      }
      case ParallelSlotData parallel -> {
        ParallelStepSlot.Builder builder =
            ParallelStepSlot.Builder.from(templateOf(data, ParallelStepTemplate.class, snapshotOf));
        builder.setId(id);
        parallel.branches().forEach(branch -> builder.addBranch(new StepSlotId(branch)));
        builder.setJoin(new StepSlotId(parallel.join()));
        yield builder.toBuild();
      }
    };
  }

  private static <T extends StepTemplate> T templateOf(
      StepSlotData data, Class<T> type, BiFunction<StepTemplateId, Long, StepTemplate> snapshotOf) {
    StepTemplate template =
        snapshotOf.apply(new StepTemplateId(data.templateId()), data.templateVersion());
    if (!type.isInstance(template)) {
      throw new IllegalArgumentException(
          "template type mismatch for slot " + data.id() + ": " + template.getClass().getName());
    }
    return type.cast(template);
  }

  private static @Nullable UUID valueOf(@Nullable StepSlotId id) {
    return id == null ? null : id.value();
  }
}
