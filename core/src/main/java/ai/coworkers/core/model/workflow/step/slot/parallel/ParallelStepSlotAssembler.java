package ai.coworkers.core.model.workflow.step.slot.parallel;

import ai.coworkers.core.model.workflow.step.slot.StepSlot;
import ai.coworkers.core.model.workflow.step.slot.StepSlotAssembler;
import ai.coworkers.core.model.workflow.step.slot.StepSlotId;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import ai.coworkers.core.model.workflow.step.template.parallel.ParallelStepTemplate;
import java.util.function.Function;

/** 병렬 템플릿을 병렬 슬롯으로 조립하는 전략 */
public final class ParallelStepSlotAssembler implements StepSlotAssembler<ParallelStepTemplate> {

  @Override
  public Class<ParallelStepTemplate> templateType() {
    return ParallelStepTemplate.class;
  }

  @Override
  public StepSlot assemble(
      ParallelStepTemplate template, StepSlotId id, Function<StepTemplateId, StepSlotId> slotIdOf) {
    ParallelStepSlot.Builder builder = ParallelStepSlot.Builder.from(template);
    builder.setId(id);
    for (StepTemplateId branch : template.branches()) {
      builder.addBranch(slotIdOf.apply(branch));
    }
    builder.setJoin(slotIdOf.apply(template.join()));
    return builder.toBuild();
  }
}
