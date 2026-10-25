package ai.coworkers.core.model.runtime.step.slot.loop;

import static org.assertj.core.api.Assertions.assertThat;

import ai.coworkers.core.model.runtime.step.slot.StepSlotId;
import ai.coworkers.core.model.template.step.LoopStepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.condition.FixedStepConditionType;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LoopStepSlotAssembler")
class LoopStepSlotAssemblerTest {

  private static final StepTemplateId BODY_TEMPLATE = new StepTemplateId(UUID.randomUUID());
  private static final StepTemplateId EXIT_TEMPLATE = new StepTemplateId(UUID.randomUUID());
  private static final StepSlotId ID = StepSlotId.generate();
  private static final StepSlotId BODY_SLOT = StepSlotId.generate();
  private static final StepSlotId EXIT_SLOT = StepSlotId.generate();
  private static final LoopStepTemplate TEMPLATE =
      new LoopStepTemplate(
          new StepTemplateId(UUID.randomUUID()),
          1L,
          FixedStepConditionType.leaf(true),
          BODY_TEMPLATE,
          EXIT_TEMPLATE);

  private final LoopStepSlotAssembler assembler = new LoopStepSlotAssembler();

  @Nested
  @DisplayName("templateType")
  class TemplateType {

    @Test
    @DisplayName("반복 템플릿 타입을 반환함")
    void returnsLoopTemplateType() {
      assertThat(assembler.templateType()).isEqualTo(LoopStepTemplate.class);
    }
  }

  @Nested
  @DisplayName("assemble")
  class Assemble {

    @Test
    @DisplayName("본문과 종료 출구를 대응하는 슬롯 식별자로 바꿔 반복 슬롯을 만듦")
    void mapsExitsToSlotIds() {
      Map<StepTemplateId, StepSlotId> slotIds =
          Map.of(BODY_TEMPLATE, BODY_SLOT, EXIT_TEMPLATE, EXIT_SLOT);

      assertThat(assembler.assemble(TEMPLATE, ID, slotIds::get))
          .isEqualTo(new LoopStepSlot(ID, TEMPLATE, BODY_SLOT, EXIT_SLOT));
    }
  }
}
