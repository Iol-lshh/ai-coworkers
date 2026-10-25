package ai.coworkers.core.model.runtime.step.slot.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.runtime.step.output.StepOutput;
import ai.coworkers.core.model.runtime.step.slot.StepSlotId;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.input.StepInput;
import ai.coworkers.core.model.template.step.task.TaskStepTemplate;
import java.util.ArrayList;
import java.util.List;
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
  private static final TaskExecutor EXECUTOR = (template, input) -> RESULT;

  private record TestTaskTemplate(StepTemplateId id, @Nullable StepTemplateId next)
      implements TaskStepTemplate {
    @Override
    public Long version() {
      return 1L;
    }
  }

  @Nested
  @DisplayName("process")
  class Process {

    @Test
    @DisplayName("작업 결과와 다음 슬롯을 담아 반환함")
    void returnsResultAndNext() {
      TaskStepSlot slot = new TaskStepSlot(ID, TEMPLATE, NEXT, EXECUTOR);

      assertThat(slot.process(INPUT)).isEqualTo(new TaskSlotOutput(RESULT, NEXT));
    }

    @Test
    @DisplayName("다음 슬롯이 없으면 다음 슬롯 없이 반환함")
    void returnsResultWithoutNext() {
      TaskStepSlot slot = new TaskStepSlot(ID, TEMPLATE, null, EXECUTOR);

      assertThat(slot.process(INPUT)).isEqualTo(new TaskSlotOutput(RESULT, null));
    }

    @Test
    @DisplayName("실행기에 작업 템플릿과 입력을 넘겨 작업을 실행함")
    void passesTemplateAndInputToExecutor() {
      List<Object> received = new ArrayList<>();
      TaskExecutor recording =
          (template, input) -> {
            received.add(template);
            received.add(input);
            return RESULT;
          };

      new TaskStepSlot(ID, TEMPLATE, NEXT, recording).process(INPUT);

      assertThat(received).containsExactly(TEMPLATE, INPUT);
    }
  }

  @Nested
  @DisplayName("toBuild")
  class ToBuild {

    @Test
    @DisplayName("설정한 값으로 슬롯을 만듦")
    void buildsWithSetValues() {
      TaskStepSlot.Builder builder = TaskStepSlot.Builder.from(TEMPLATE, EXECUTOR);
      builder.setId(ID);
      builder.setNext(NEXT);

      assertThat(builder.toBuild()).isEqualTo(new TaskStepSlot(ID, TEMPLATE, NEXT, EXECUTOR));
    }

    @Test
    @DisplayName("다음 슬롯을 설정하지 않으면 다음 슬롯 없이 만듦")
    void buildsWithoutNext() {
      TaskStepSlot.Builder builder = TaskStepSlot.Builder.from(TEMPLATE, EXECUTOR);
      builder.setId(ID);

      assertThat(builder.toBuild().next()).isNull();
    }

    @Test
    @DisplayName("식별자를 설정하지 않으면 거부함")
    void rejectsMissingId() {
      TaskStepSlot.Builder builder = TaskStepSlot.Builder.from(TEMPLATE, EXECUTOR);

      assertThatThrownBy(builder::toBuild)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("id must be set");
    }
  }
}
