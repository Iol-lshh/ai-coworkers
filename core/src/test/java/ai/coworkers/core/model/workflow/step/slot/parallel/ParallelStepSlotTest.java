package ai.coworkers.core.model.workflow.step.slot.parallel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.output.ForkOutput;
import ai.coworkers.core.model.workflow.step.slot.StepSlotId;
import ai.coworkers.core.model.workflow.step.template.ParallelStepTemplate;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ParallelStepSlot")
class ParallelStepSlotTest {

  private static final StepInput INPUT = StepInput.empty();
  private static final ParallelStepTemplate TEMPLATE =
      new ParallelStepTemplate(
          new StepTemplateId(UUID.randomUUID()),
          1L,
          List.of(new StepTemplateId(UUID.randomUUID()), new StepTemplateId(UUID.randomUUID())),
          new StepTemplateId(UUID.randomUUID()));
  private static final StepSlotId ID = StepSlotId.generate();
  private static final StepSlotId FIRST = StepSlotId.generate();
  private static final StepSlotId SECOND = StepSlotId.generate();
  private static final StepSlotId JOIN = StepSlotId.generate();

  @Nested
  @DisplayName("process")
  class Process {

    @Test
    @DisplayName("동시에 시작할 갈래와 합류 슬롯을 담아 반환함")
    void returnsBranchesAndJoin() {
      ParallelStepSlot slot = new ParallelStepSlot(ID, TEMPLATE, List.of(FIRST, SECOND), JOIN);

      assertThat(slot.process(INPUT)).isEqualTo(new ForkOutput(List.of(FIRST, SECOND), JOIN));
    }
  }

  @Nested
  @DisplayName("toBuild")
  class ToBuild {

    @Test
    @DisplayName("추가한 순서대로 갈래를 보관한 슬롯을 만듦")
    void buildsWithBranchesInOrder() {
      ParallelStepSlot.Builder builder = ParallelStepSlot.Builder.from(TEMPLATE);
      builder.setId(ID);
      builder.addBranch(SECOND).addBranch(FIRST);
      builder.setJoin(JOIN);

      assertThat(builder.toBuild())
          .isEqualTo(new ParallelStepSlot(ID, TEMPLATE, List.of(SECOND, FIRST), JOIN));
    }

    @Test
    @DisplayName("만든 뒤 빌더에 갈래를 추가해도 슬롯은 바뀌지 않음")
    void copiesBranches() {
      ParallelStepSlot.Builder builder = ParallelStepSlot.Builder.from(TEMPLATE);
      builder.setId(ID);
      builder.addBranch(FIRST);
      builder.setJoin(JOIN);
      ParallelStepSlot slot = builder.toBuild();

      builder.addBranch(SECOND);

      assertThat(slot.branches()).containsExactly(FIRST);
    }

    @ParameterizedTest(name = "missing [{0}]")
    @ValueSource(strings = {"id", "join"})
    @DisplayName("필수 값이 빠지면 거부함")
    void rejectsMissingValue(String missing) {
      ParallelStepSlot.Builder builder = ParallelStepSlot.Builder.from(TEMPLATE);
      builder.addBranch(FIRST);
      if (!missing.equals("id")) {
        builder.setId(ID);
      }
      if (!missing.equals("join")) {
        builder.setJoin(JOIN);
      }

      assertThatThrownBy(builder::toBuild)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("id and join must be set");
    }
  }
}
