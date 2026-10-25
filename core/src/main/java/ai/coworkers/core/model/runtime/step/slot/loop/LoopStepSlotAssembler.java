package ai.coworkers.core.model.runtime.step.slot.loop;

import ai.coworkers.core.model.runtime.step.slot.StepSlot;
import ai.coworkers.core.model.runtime.step.slot.StepSlotAssembler;
import ai.coworkers.core.model.runtime.step.slot.StepSlotId;
import ai.coworkers.core.model.template.step.LoopStepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import java.util.function.Function;

/** 반복 템플릿을 반복 슬롯으로 조립하는 전략 */
public final class LoopStepSlotAssembler implements StepSlotAssembler<LoopStepTemplate> {

  @Override
  public Class<LoopStepTemplate> templateType() {
    return LoopStepTemplate.class;
  }

  @Override
  public StepSlot assemble(
      LoopStepTemplate template, StepSlotId id, Function<StepTemplateId, StepSlotId> slotIdOf) {
    LoopStepSlot.Builder builder = LoopStepSlot.Builder.from(template);
    builder.setId(id);
    builder.setBody(slotIdOf.apply(template.body()));
    builder.setExit(slotIdOf.apply(template.exit()));
    return builder.toBuild();
  }
}
