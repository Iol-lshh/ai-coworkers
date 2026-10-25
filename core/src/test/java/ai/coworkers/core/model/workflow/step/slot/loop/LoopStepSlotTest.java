package ai.coworkers.core.model.workflow.step.slot.loop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.workflow.step.condition.FixedStepConditionType;
import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.output.NextSlotOutput;
import ai.coworkers.core.model.workflow.step.slot.StepSlotId;
import ai.coworkers.core.model.workflow.step.template.LoopStepTemplate;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("LoopStepSlot")
class LoopStepSlotTest {

  private static final StepInput INPUT = StepInput.empty();
  private static final StepSlotId ID = StepSlotId.generate();
  private static final StepSlotId BODY = StepSlotId.generate();
  private static final StepSlotId EXIT = StepSlotId.generate();

  private static LoopStepTemplate template(boolean satisfied) {
    return new LoopStepTemplate(
        new StepTemplateId(UUID.randomUUID()),
        1L,
        FixedStepConditionType.leaf(satisfied),
        new StepTemplateId(UUID.randomUUID()),
        new StepTemplateId(UUID.randomUUID()));
  }

  @Nested
  @DisplayName("process")
  class Process {

    @ParameterizedTest(name = "satisfied={0} → {1}")
    @CsvSource({"true, body", "false, exit"})
    @DisplayName("조건을 만족하면 본문을, 만족하지 않으면 종료 슬롯을 반환함")
    void returnsNextByCondition(boolean satisfied, String expectedExit) {
      LoopStepSlot slot = new LoopStepSlot(ID, template(satisfied), BODY, EXIT);
      StepSlotId expected = expectedExit.equals("body") ? BODY : EXIT;

      assertThat(slot.process(INPUT)).isEqualTo(new NextSlotOutput(expected));
    }
  }

  @Nested
  @DisplayName("toBuild")
  class ToBuild {

    @Test
    @DisplayName("설정한 값으로 슬롯을 만듦")
    void buildsWithSetValues() {
      LoopStepTemplate template = template(true);
      LoopStepSlot.Builder builder = LoopStepSlot.Builder.from(template);
      builder.setId(ID);
      builder.setBody(BODY);
      builder.setExit(EXIT);

      assertThat(builder.toBuild()).isEqualTo(new LoopStepSlot(ID, template, BODY, EXIT));
    }

    @ParameterizedTest(name = "missing [{0}]")
    @ValueSource(strings = {"id", "body", "exit"})
    @DisplayName("필수 값이 빠지면 거부함")
    void rejectsMissingValue(String missing) {
      LoopStepSlot.Builder builder = LoopStepSlot.Builder.from(template(true));
      if (!missing.equals("id")) {
        builder.setId(ID);
      }
      if (!missing.equals("body")) {
        builder.setBody(BODY);
      }
      if (!missing.equals("exit")) {
        builder.setExit(EXIT);
      }

      assertThatThrownBy(builder::toBuild)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("id, body and exit must be set");
    }
  }
}
