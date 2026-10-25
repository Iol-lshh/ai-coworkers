package ai.coworkers.core.model.workflow.step.slot.branch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.workflow.step.StepCondition;
import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.output.NextSlotOutput;
import ai.coworkers.core.model.workflow.step.slot.StepSlotId;
import ai.coworkers.core.model.workflow.step.template.BranchStepTemplate;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("BranchStepSlot")
class BranchStepSlotTest {

  private static final StepInput INPUT = new StepInput() {};
  private static final StepSlotId ID = StepSlotId.generate();
  private static final StepSlotId WHEN_SATISFIED = StepSlotId.generate();
  private static final StepSlotId OTHERWISE = StepSlotId.generate();

  private record FixedCondition(boolean satisfied) implements StepCondition {
    @Override
    public UUID id() {
      return new UUID(0, 0);
    }

    @Override
    public boolean isSatisfiedBy(StepInput candidate) {
      return satisfied;
    }
  }

  private static BranchStepTemplate template(boolean satisfied) {
    return new BranchStepTemplate(
        new StepTemplateId(UUID.randomUUID()),
        1L,
        new FixedCondition(satisfied),
        new StepTemplateId(UUID.randomUUID()),
        new StepTemplateId(UUID.randomUUID()));
  }

  @Nested
  @DisplayName("process")
  class Process {

    @ParameterizedTest(name = "satisfied={0} → {1}")
    @CsvSource({"true, whenSatisfied", "false, otherwise"})
    @DisplayName("조건 판정 결과에 맞는 다음 슬롯을 반환함")
    void returnsNextByCondition(boolean satisfied, String expectedExit) {
      BranchStepSlot slot = new BranchStepSlot(ID, template(satisfied), WHEN_SATISFIED, OTHERWISE);
      StepSlotId expected = expectedExit.equals("whenSatisfied") ? WHEN_SATISFIED : OTHERWISE;

      assertThat(slot.process(INPUT)).isEqualTo(new NextSlotOutput(expected));
    }
  }

  @Nested
  @DisplayName("toBuild")
  class ToBuild {

    @Test
    @DisplayName("설정한 값으로 슬롯을 만듦")
    void buildsWithSetValues() {
      BranchStepTemplate template = template(true);
      BranchStepSlot.Builder builder = BranchStepSlot.Builder.from(template);
      builder.setId(ID);
      builder.setWhenSatisfied(WHEN_SATISFIED);
      builder.setOtherwise(OTHERWISE);

      assertThat(builder.toBuild())
          .isEqualTo(new BranchStepSlot(ID, template, WHEN_SATISFIED, OTHERWISE));
    }

    @ParameterizedTest(name = "missing [{0}]")
    @ValueSource(strings = {"id", "whenSatisfied", "otherwise"})
    @DisplayName("필수 값이 빠지면 거부함")
    void rejectsMissingValue(String missing) {
      BranchStepSlot.Builder builder = BranchStepSlot.Builder.from(template(true));
      if (!missing.equals("id")) {
        builder.setId(ID);
      }
      if (!missing.equals("whenSatisfied")) {
        builder.setWhenSatisfied(WHEN_SATISFIED);
      }
      if (!missing.equals("otherwise")) {
        builder.setOtherwise(OTHERWISE);
      }

      assertThatThrownBy(builder::toBuild)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("id, whenSatisfied and otherwise must be set");
    }
  }
}
