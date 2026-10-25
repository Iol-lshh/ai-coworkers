package ai.coworkers.core.model.workflow.step.slot.branch;

import ai.coworkers.core.model.workflow.step.slot.StepSlot;
import ai.coworkers.core.model.workflow.step.slot.StepSlotAssembler;
import ai.coworkers.core.model.workflow.step.slot.StepSlotId;
import ai.coworkers.core.model.workflow.step.template.BranchStepTemplate;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import java.util.function.Function;

/** 분기 템플릿을 분기 슬롯으로 조립하는 전략 */
public final class BranchStepSlotAssembler implements StepSlotAssembler<BranchStepTemplate> {

  @Override
  public Class<BranchStepTemplate> templateType() {
    return BranchStepTemplate.class;
  }

  @Override
  public StepSlot assemble(
      BranchStepTemplate template, StepSlotId id, Function<StepTemplateId, StepSlotId> slotIdOf) {
    BranchStepSlot.Builder builder = BranchStepSlot.Builder.from(template);
    builder.setId(id);
    builder.setWhenSatisfied(slotIdOf.apply(template.whenSatisfied()));
    builder.setOtherwise(slotIdOf.apply(template.otherwise()));
    return builder.toBuild();
  }
}
