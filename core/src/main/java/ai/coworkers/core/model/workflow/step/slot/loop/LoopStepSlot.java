package ai.coworkers.core.model.workflow.step.slot.loop;

import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.output.NextSlotOutput;
import ai.coworkers.core.model.workflow.step.output.StepOutput;
import ai.coworkers.core.model.workflow.step.slot.StepSlot;
import ai.coworkers.core.model.workflow.step.slot.StepSlotId;
import ai.coworkers.core.model.workflow.step.template.LoopStepTemplate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * 반복 템플릿을 놓는 슬롯
 *
 * @param id 슬롯 식별자
 * @param template 반복 템플릿 식별자
 * @param body 반복할 때 이어지는 슬롯
 * @param exit 반복을 끝낼 때 이어지는 슬롯
 */
public record LoopStepSlot(
    StepSlotId id, LoopStepTemplate template, StepSlotId body, StepSlotId exit)
    implements StepSlot {
  @Override
  public StepOutput process(StepInput input) {
    return new NextSlotOutput(template.condition().isSatisfiedBy(input) ? body : exit);
  }

  @Setter
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final LoopStepTemplate template;
    @Nullable private StepSlotId id;
    @Nullable private StepSlotId body;
    @Nullable private StepSlotId exit;

    public static Builder from(LoopStepTemplate template) {
      return new Builder(template);
    }

    public LoopStepSlot toBuild() {
      if (id == null || body == null || exit == null) {
        throw new NullPointerException("id, body and exit must be set");
      }
      return new LoopStepSlot(this.id, this.template, this.body, this.exit);
    }
  }
}
