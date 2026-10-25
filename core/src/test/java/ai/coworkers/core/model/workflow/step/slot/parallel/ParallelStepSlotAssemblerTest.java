package ai.coworkers.core.model.workflow.step.slot.parallel;

import static org.assertj.core.api.Assertions.assertThat;

import ai.coworkers.core.model.workflow.step.slot.StepSlotId;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import ai.coworkers.core.model.workflow.step.template.parallel.ParallelStepTemplate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ParallelStepSlotAssembler")
class ParallelStepSlotAssemblerTest {

  private static final StepTemplateId FIRST_TEMPLATE = new StepTemplateId(UUID.randomUUID());
  private static final StepTemplateId SECOND_TEMPLATE = new StepTemplateId(UUID.randomUUID());
  private static final StepTemplateId JOIN_TEMPLATE = new StepTemplateId(UUID.randomUUID());
  private static final StepSlotId ID = StepSlotId.generate();
  private static final StepSlotId FIRST_SLOT = StepSlotId.generate();
  private static final StepSlotId SECOND_SLOT = StepSlotId.generate();
  private static final StepSlotId JOIN_SLOT = StepSlotId.generate();
  private static final ParallelStepTemplate TEMPLATE =
      new ParallelStepTemplate(
          new StepTemplateId(UUID.randomUUID()),
          1L,
          List.of(SECOND_TEMPLATE, FIRST_TEMPLATE),
          JOIN_TEMPLATE);

  private final ParallelStepSlotAssembler assembler = new ParallelStepSlotAssembler();

  @Nested
  @DisplayName("templateType")
  class TemplateType {

    @Test
    @DisplayName("병렬 템플릿 타입을 반환함")
    void returnsParallelTemplateType() {
      assertThat(assembler.templateType()).isEqualTo(ParallelStepTemplate.class);
    }
  }

  @Nested
  @DisplayName("assemble")
  class Assemble {

    @Test
    @DisplayName("갈래를 템플릿 순서대로, 합류를 대응하는 슬롯 식별자로 바꿔 병렬 슬롯을 만듦")
    void mapsBranchesInOrderAndJoin() {
      Map<StepTemplateId, StepSlotId> slotIds =
          Map.of(
              FIRST_TEMPLATE, FIRST_SLOT, SECOND_TEMPLATE, SECOND_SLOT, JOIN_TEMPLATE, JOIN_SLOT);

      assertThat(assembler.assemble(TEMPLATE, ID, slotIds::get))
          .isEqualTo(
              new ParallelStepSlot(ID, TEMPLATE, List.of(SECOND_SLOT, FIRST_SLOT), JOIN_SLOT));
    }
  }
}
