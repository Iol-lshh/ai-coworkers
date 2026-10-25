package ai.coworkers.core.model.runtime.step.slot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.runtime.step.output.StepOutput;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.BranchSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.LoopSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.ParallelSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.TaskSlotData;
import ai.coworkers.core.model.runtime.step.slot.branch.BranchStepSlot;
import ai.coworkers.core.model.runtime.step.slot.loop.LoopStepSlot;
import ai.coworkers.core.model.runtime.step.slot.parallel.ParallelStepSlot;
import ai.coworkers.core.model.runtime.step.slot.task.TaskStepSlot;
import ai.coworkers.core.model.template.step.BranchStepTemplate;
import ai.coworkers.core.model.template.step.LoopStepTemplate;
import ai.coworkers.core.model.template.step.ParallelStepTemplate;
import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.condition.FixedStepConditionType;
import ai.coworkers.core.model.template.step.input.StepInput;
import ai.coworkers.core.model.template.step.task.TaskStepTemplate;
import java.util.List;
import java.util.function.BiFunction;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("StepSlotCodec")
class StepSlotCodecTest {

  private static final StepSlotCodec CODEC = new StepSlotCodec();
  private static final StepOutput RESULT = new StepOutput() {};
  private static final StepSlotId ID = StepSlotId.generate();
  private static final StepSlotId FIRST = StepSlotId.generate();
  private static final StepSlotId SECOND = StepSlotId.generate();
  private static final StepTemplateId NEXT = StepTemplateId.generate();

  private static final BranchStepTemplate BRANCH_TEMPLATE =
      new BranchStepTemplate(
          StepTemplateId.generate(), 2L, FixedStepConditionType.leaf(true), NEXT, NEXT);
  private static final LoopStepTemplate LOOP_TEMPLATE =
      new LoopStepTemplate(
          StepTemplateId.generate(), 3L, FixedStepConditionType.leaf(true), NEXT, NEXT);
  private static final ParallelStepTemplate PARALLEL_TEMPLATE =
      new ParallelStepTemplate(StepTemplateId.generate(), 4L, List.of(NEXT), NEXT);

  private static final BranchStepSlot BRANCH =
      new BranchStepSlot(ID, BRANCH_TEMPLATE, FIRST, SECOND);
  private static final LoopStepSlot LOOP = new LoopStepSlot(ID, LOOP_TEMPLATE, FIRST, SECOND);
  private static final ParallelStepSlot PARALLEL =
      new ParallelStepSlot(ID, PARALLEL_TEMPLATE, List.of(FIRST, SECOND), FIRST);

  private static final BiFunction<StepTemplateId, Long, StepTemplate> SNAPSHOT_OF =
      (id, version) ->
          List.<StepTemplate>of(BRANCH_TEMPLATE, LOOP_TEMPLATE, PARALLEL_TEMPLATE).stream()
              .filter(template -> template.id().equals(id) && template.version().equals(version))
              .findFirst()
              .orElseThrow();

  private record TestTaskTemplate(StepTemplateId id) implements TaskStepTemplate {
    @Override
    public Long version() {
      return 1L;
    }

    @Override
    public @Nullable StepTemplateId next() {
      return null;
    }
  }

  private record UnknownSlot(StepSlotId id, StepTemplate template) implements StepSlot {
    @Override
    public StepOutput process(StepInput input) {
      return RESULT;
    }
  }

  @Nested
  @DisplayName("encode")
  class Encode {

    @Test
    @DisplayName("슬롯 종류마다 템플릿 식별 값·버전과 출구 슬롯 식별 값을 담음")
    void encodesEachKind() {
      TestTaskTemplate task = new TestTaskTemplate(StepTemplateId.generate());

      assertThat(CODEC.encode(new TaskStepSlot(ID, task, null, (template, input) -> RESULT)))
          .isEqualTo(new TaskSlotData(ID.value(), task.id().value(), 1L, null));
      assertThat(CODEC.encode(BRANCH))
          .isEqualTo(
              new BranchSlotData(
                  ID.value(), BRANCH_TEMPLATE.id().value(), 2L, FIRST.value(), SECOND.value()));
      assertThat(CODEC.encode(LOOP))
          .isEqualTo(
              new LoopSlotData(
                  ID.value(), LOOP_TEMPLATE.id().value(), 3L, FIRST.value(), SECOND.value()));
      assertThat(CODEC.encode(PARALLEL))
          .isEqualTo(
              new ParallelSlotData(
                  ID.value(),
                  PARALLEL_TEMPLATE.id().value(),
                  4L,
                  List.of(FIRST.value(), SECOND.value()),
                  FIRST.value()));
    }

    @Test
    @DisplayName("저장 형태가 없는 슬롯 종류면 거부함")
    void rejectsUnknownSlot() {
      UnknownSlot slot = new UnknownSlot(ID, PARALLEL_TEMPLATE);

      assertThatThrownBy(() -> CODEC.encode(slot))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("unsupported slot type: " + UnknownSlot.class.getName());
    }
  }

  @Nested
  @DisplayName("decode")
  class Decode {

    @Test
    @DisplayName("기록된 버전의 스냅샷과 출구로 같은 슬롯을 복원함")
    void decodesEncoded() {
      assertThat(List.<StepSlot>of(BRANCH, LOOP, PARALLEL))
          .allSatisfy(
              slot -> assertThat(CODEC.decode(CODEC.encode(slot), SNAPSHOT_OF)).isEqualTo(slot));
    }

    @Test
    @DisplayName("작업 슬롯은 복원을 거부함")
    void rejectsTaskSlot() {
      TaskSlotData task = new TaskSlotData(ID.value(), NEXT.value(), 1L, null);

      assertThatThrownBy(() -> CODEC.decode(task, SNAPSHOT_OF))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("task slot restore unsupported: " + ID.value());
    }

    @Test
    @DisplayName("스냅샷의 종류가 슬롯과 맞지 않으면 거부함")
    void rejectsTemplateTypeMismatch() {
      BranchSlotData mismatched =
          new BranchSlotData(
              ID.value(), LOOP_TEMPLATE.id().value(), 3L, FIRST.value(), SECOND.value());

      assertThatThrownBy(() -> CODEC.decode(mismatched, SNAPSHOT_OF))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageStartingWith("template type mismatch for slot " + ID.value());
    }
  }
}
