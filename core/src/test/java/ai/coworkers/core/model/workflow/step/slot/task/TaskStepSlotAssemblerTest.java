package ai.coworkers.core.model.workflow.step.slot.task;

import static org.assertj.core.api.Assertions.assertThat;

import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.output.StepOutput;
import ai.coworkers.core.model.workflow.step.slot.StepSlotId;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import ai.coworkers.core.model.workflow.step.template.task.TaskStepTemplate;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TaskStepSlotAssembler")
class TaskStepSlotAssemblerTest {

  private static final StepOutput RESULT = new StepOutput() {};
  private static final StepTemplateId NEXT_TEMPLATE = new StepTemplateId(UUID.randomUUID());
  private static final StepSlotId ID = StepSlotId.generate();
  private static final StepSlotId NEXT_SLOT = StepSlotId.generate();
  private static final Function<StepTemplateId, StepSlotId> SLOT_ID_OF =
      Map.of(NEXT_TEMPLATE, NEXT_SLOT)::get;

  private final TaskStepSlotAssembler assembler = new TaskStepSlotAssembler();

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
  @DisplayName("templateType")
  class TemplateType {

    @Test
    @DisplayName("작업 템플릿 타입을 반환함")
    void returnsTaskTemplateType() {
      assertThat(assembler.templateType()).isEqualTo(TaskStepTemplate.class);
    }
  }

  @Nested
  @DisplayName("assemble")
  class Assemble {

    @Test
    @DisplayName("다음 템플릿을 대응하는 슬롯 식별자로 바꿔 작업 슬롯을 만듦")
    void mapsNextToSlotId() {
      TestTaskTemplate template =
          new TestTaskTemplate(new StepTemplateId(UUID.randomUUID()), NEXT_TEMPLATE);

      assertThat(assembler.assemble(template, ID, SLOT_ID_OF))
          .isEqualTo(new TaskStepSlot(ID, template, NEXT_SLOT));
    }

    @Test
    @DisplayName("다음 템플릿이 없으면 다음 슬롯 없이 만듦")
    void keepsMissingNext() {
      TestTaskTemplate template = new TestTaskTemplate(new StepTemplateId(UUID.randomUUID()), null);
      Function<StepTemplateId, StepSlotId> unused =
          templateId -> {
            throw new AssertionError("slotIdOf must not be called");
          };

      assertThat(assembler.assemble(template, ID, unused))
          .isEqualTo(new TaskStepSlot(ID, template, null));
    }
  }
}
