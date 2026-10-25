package ai.coworkers.core.model.workflow.step.slot.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.output.StepOutput;
import ai.coworkers.core.model.workflow.step.slot.StepSlotId;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import ai.coworkers.core.model.workflow.step.template.task.TaskStepTemplate;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TaskStepSlot")
class TaskStepSlotTest {

  private static final StepInput INPUT = StepInput.empty();
  private static final StepOutput RESULT = new StepOutput() {};
  private static final TaskStepTemplate TEMPLATE =
      new TestTaskTemplate(new StepTemplateId(UUID.randomUUID()), null);
  private static final StepSlotId ID = StepSlotId.generate();
  private static final StepSlotId NEXT = StepSlotId.generate();

  private record TestTaskTemplate(StepTemplateId id, @Nullable StepTemplateId next)
      implements TaskStepTemplate {
    @Override
    public Long version() {
      return 1L;
    }

    @Override
    public StepOutput process(StepInput input) {
      return RESULT;
    }
  }

  @Nested
  @DisplayName("process")
  class Process {

    @Test
    @DisplayName("작업 결과와 다음 슬롯을 담아 반환함")
    void returnsResultAndNext() {
      TaskStepSlot slot = new TaskStepSlot(ID, TEMPLATE, NEXT);

      assertThat(slot.process(INPUT)).isEqualTo(new TaskSlotOutput(RESULT, NEXT));
    }

    @Test
    @DisplayName("다음 슬롯이 없으면 다음 슬롯 없이 반환함")
    void returnsResultWithoutNext() {
      TaskStepSlot slot = new TaskStepSlot(ID, TEMPLATE, null);

      assertThat(slot.process(INPUT)).isEqualTo(new TaskSlotOutput(RESULT, null));
    }
  }

  @Nested
  @DisplayName("toBuild")
  class ToBuild {

    @Test
    @DisplayName("설정한 값으로 슬롯을 만듦")
    void buildsWithSetValues() {
      TaskStepSlot.Builder builder = TaskStepSlot.Builder.from(TEMPLATE);
      builder.setId(ID);
      builder.setNext(NEXT);

      assertThat(builder.toBuild()).isEqualTo(new TaskStepSlot(ID, TEMPLATE, NEXT));
    }

    @Test
    @DisplayName("다음 슬롯을 설정하지 않으면 다음 슬롯 없이 만듦")
    void buildsWithoutNext() {
      TaskStepSlot.Builder builder = TaskStepSlot.Builder.from(TEMPLATE);
      builder.setId(ID);

      assertThat(builder.toBuild().next()).isNull();
    }

    @Test
    @DisplayName("식별자를 설정하지 않으면 거부함")
    void rejectsMissingId() {
      TaskStepSlot.Builder builder = TaskStepSlot.Builder.from(TEMPLATE);

      assertThatThrownBy(builder::toBuild)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("id must be set");
    }
  }
}
