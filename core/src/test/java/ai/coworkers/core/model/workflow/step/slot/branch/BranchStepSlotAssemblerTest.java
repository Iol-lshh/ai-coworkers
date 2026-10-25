package ai.coworkers.core.model.workflow.step.slot.branch;

import static org.assertj.core.api.Assertions.assertThat;

import ai.coworkers.core.model.workflow.step.StepCondition;
import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.slot.StepSlotId;
import ai.coworkers.core.model.workflow.step.template.BranchStepTemplate;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("BranchStepSlotAssembler")
class BranchStepSlotAssemblerTest {

  private static final StepTemplateId WHEN_SATISFIED_TEMPLATE =
      new StepTemplateId(UUID.randomUUID());
  private static final StepTemplateId OTHERWISE_TEMPLATE = new StepTemplateId(UUID.randomUUID());
  private static final StepSlotId ID = StepSlotId.generate();
  private static final StepSlotId WHEN_SATISFIED_SLOT = StepSlotId.generate();
  private static final StepSlotId OTHERWISE_SLOT = StepSlotId.generate();
  private static final BranchStepTemplate TEMPLATE =
      new BranchStepTemplate(
          new StepTemplateId(UUID.randomUUID()),
          1L,
          new AlwaysSatisfied(),
          WHEN_SATISFIED_TEMPLATE,
          OTHERWISE_TEMPLATE);

  private final BranchStepSlotAssembler assembler = new BranchStepSlotAssembler();

  private record AlwaysSatisfied() implements StepCondition {
    @Override
    public UUID id() {
      return new UUID(0, 0);
    }

    @Override
    public boolean isSatisfiedBy(StepInput candidate) {
      return true;
    }
  }

  @Nested
  @DisplayName("templateType")
  class TemplateType {

    @Test
    @DisplayName("분기 템플릿 타입을 반환함")
    void returnsBranchTemplateType() {
      assertThat(assembler.templateType()).isEqualTo(BranchStepTemplate.class);
    }
  }

  @Nested
  @DisplayName("assemble")
  class Assemble {

    @Test
    @DisplayName("두 출구를 대응하는 슬롯 식별자로 바꿔 분기 슬롯을 만듦")
    void mapsExitsToSlotIds() {
      Map<StepTemplateId, StepSlotId> slotIds =
          Map.of(WHEN_SATISFIED_TEMPLATE, WHEN_SATISFIED_SLOT, OTHERWISE_TEMPLATE, OTHERWISE_SLOT);

      assertThat(assembler.assemble(TEMPLATE, ID, slotIds::get))
          .isEqualTo(new BranchStepSlot(ID, TEMPLATE, WHEN_SATISFIED_SLOT, OTHERWISE_SLOT));
    }
  }
}
