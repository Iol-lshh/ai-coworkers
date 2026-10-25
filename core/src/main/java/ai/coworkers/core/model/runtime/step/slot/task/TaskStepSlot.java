package ai.coworkers.core.model.runtime.step.slot.task;

import ai.coworkers.core.model.runtime.step.output.StepOutput;
import ai.coworkers.core.model.runtime.step.slot.StepSlot;
import ai.coworkers.core.model.runtime.step.slot.StepSlotId;
import ai.coworkers.core.model.template.step.input.StepInput;
import ai.coworkers.core.model.template.step.task.TaskStepTemplate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * 작업 템플릿을 놓는 슬롯
 *
 * @param id 슬롯 식별자
 * @param template 작업 템플릿 식별자
 * @param next 다음 슬롯. 없으면 흐름이 끝남
 * @param executor 작업 실행기
 */
public record TaskStepSlot(
    StepSlotId id, TaskStepTemplate template, @Nullable StepSlotId next, TaskExecutor executor)
    implements StepSlot {
  @Override
  public StepOutput process(StepInput input) {
    return new TaskSlotOutput(executor.execute(template, input), next);
  }

  @Setter
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final TaskStepTemplate template;
    private final TaskExecutor executor;
    @Nullable private StepSlotId id;
    @Nullable private StepSlotId next;

    public static Builder from(TaskStepTemplate template, TaskExecutor executor) {
      return new Builder(template, executor);
    }

    public TaskStepSlot toBuild() {
      if (id == null) {
        throw new NullPointerException("id must be set");
      }
      return new TaskStepSlot(this.id, this.template, this.next, this.executor);
    }
  }
}
