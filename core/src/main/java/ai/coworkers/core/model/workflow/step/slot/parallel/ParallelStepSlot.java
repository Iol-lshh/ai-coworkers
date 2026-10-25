package ai.coworkers.core.model.workflow.step.slot.parallel;

import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.output.ForkOutput;
import ai.coworkers.core.model.workflow.step.output.StepOutput;
import ai.coworkers.core.model.workflow.step.slot.StepSlot;
import ai.coworkers.core.model.workflow.step.slot.StepSlotId;
import ai.coworkers.core.model.workflow.step.template.ParallelStepTemplate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * 병렬 템플릿을 놓는 슬롯
 *
 * @param id 슬롯 식별자
 * @param template 병렬 템플릿
 * @param branches 동시에 시작하는 슬롯
 * @param join 모든 갈래가 끝난 뒤 이어지는 슬롯
 */
public record ParallelStepSlot(
    StepSlotId id, ParallelStepTemplate template, List<StepSlotId> branches, StepSlotId join)
    implements StepSlot {
  @Override
  public StepOutput process(StepInput input) {
    return new ForkOutput(branches, join);
  }

  @Setter
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final ParallelStepTemplate template;
    private final List<StepSlotId> branches = new ArrayList<>();
    @Nullable private StepSlotId id;
    @Nullable private StepSlotId join;

    public static Builder from(ParallelStepTemplate template) {
      return new Builder(template);
    }

    public Builder addBranch(StepSlotId branch) {
      branches.add(branch);
      return this;
    }

    public ParallelStepSlot toBuild() {
      if (id == null || join == null) {
        throw new NullPointerException("id and join must be set");
      }
      return new ParallelStepSlot(this.id, this.template, List.copyOf(this.branches), this.join);
    }
  }
}
