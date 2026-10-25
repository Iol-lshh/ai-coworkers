package ai.coworkers.core.model.workflow.step.slot.branch;

import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.output.NextSlotOutput;
import ai.coworkers.core.model.workflow.step.output.StepOutput;
import ai.coworkers.core.model.workflow.step.slot.StepSlot;
import ai.coworkers.core.model.workflow.step.slot.StepSlotId;
import ai.coworkers.core.model.workflow.step.template.BranchStepTemplate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * 분기 템플릿을 놓는 슬롯
 *
 * @param id 슬롯 식별자
 * @param template 분기 템플릿 식별자
 * @param whenSatisfied 조건을 만족할 때 이어지는 슬롯
 * @param otherwise 조건을 만족하지 않을 때 이어지는 슬롯
 */
public record BranchStepSlot(
    StepSlotId id, BranchStepTemplate template, StepSlotId whenSatisfied, StepSlotId otherwise)
    implements StepSlot {
  @Override
  public StepOutput process(StepInput input) {
    return new NextSlotOutput(
        template.condition().isSatisfiedBy(input) ? whenSatisfied : otherwise);
  }

  @Setter
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final BranchStepTemplate template;
    @Nullable private StepSlotId id;
    @Nullable private StepSlotId whenSatisfied;
    @Nullable private StepSlotId otherwise;

    public static Builder from(BranchStepTemplate template) {
      return new Builder(template);
    }

    public BranchStepSlot toBuild() {
      if (id == null || whenSatisfied == null || otherwise == null) {
        throw new NullPointerException("id, whenSatisfied and otherwise must be set");
      }
      return new BranchStepSlot(this.id, this.template, this.whenSatisfied, this.otherwise);
    }
  }
}
