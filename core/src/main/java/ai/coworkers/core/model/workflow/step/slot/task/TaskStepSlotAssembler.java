package ai.coworkers.core.model.workflow.step.slot.task;

import ai.coworkers.core.model.workflow.step.slot.StepSlot;
import ai.coworkers.core.model.workflow.step.slot.StepSlotAssembler;
import ai.coworkers.core.model.workflow.step.slot.StepSlotId;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import ai.coworkers.core.model.workflow.step.template.task.TaskStepTemplate;
import java.util.function.Function;

/** 작업 템플릿을 작업 슬롯으로 조립하는 전략 */
public final class TaskStepSlotAssembler implements StepSlotAssembler<TaskStepTemplate> {

  @Override
  public Class<TaskStepTemplate> templateType() {
    return TaskStepTemplate.class;
  }

  @Override
  public StepSlot assemble(
      TaskStepTemplate template, StepSlotId id, Function<StepTemplateId, StepSlotId> slotIdOf) {
    TaskStepSlot.Builder builder = TaskStepSlot.Builder.from(template);
    builder.setId(id);
    StepTemplateId next = template.next();
    if (next != null) {
      builder.setNext(slotIdOf.apply(next));
    }
    return builder.toBuild();
  }
}
