package ai.coworkers.core.model.runtime.step.slot.task;

import ai.coworkers.core.model.runtime.step.slot.StepSlot;
import ai.coworkers.core.model.runtime.step.slot.StepSlotAssembler;
import ai.coworkers.core.model.runtime.step.slot.StepSlotId;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.task.TaskStepTemplate;
import java.util.function.Function;

/** 작업 템플릿을 작업 슬롯으로 조립하는 전략. 조립한 슬롯에 작업 실행기를 넘김. */
public final class TaskStepSlotAssembler implements StepSlotAssembler<TaskStepTemplate> {

  private final TaskExecutor executor;

  /**
   * 조립한 슬롯이 쓸 작업 실행기를 받음.
   *
   * @param executor 작업 실행기
   */
  public TaskStepSlotAssembler(TaskExecutor executor) {
    this.executor = executor;
  }

  @Override
  public Class<TaskStepTemplate> templateType() {
    return TaskStepTemplate.class;
  }

  @Override
  public StepSlot assemble(
      TaskStepTemplate template, StepSlotId id, Function<StepTemplateId, StepSlotId> slotIdOf) {
    TaskStepSlot.Builder builder = TaskStepSlot.Builder.from(template, executor);
    builder.setId(id);
    StepTemplateId next = template.next();
    if (next != null) {
      builder.setNext(slotIdOf.apply(next));
    }
    return builder.toBuild();
  }
}
