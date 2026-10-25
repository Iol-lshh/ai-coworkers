package ai.coworkers.core.model.workflow.step.slot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.workflow.step.DefaultStepType;
import ai.coworkers.core.model.workflow.step.StepType;
import ai.coworkers.core.model.workflow.step.condition.FixedStepConditionType;
import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.output.StepOutput;
import ai.coworkers.core.model.workflow.step.slot.branch.BranchStepSlot;
import ai.coworkers.core.model.workflow.step.slot.branch.BranchStepSlotAssembler;
import ai.coworkers.core.model.workflow.step.slot.loop.LoopStepSlot;
import ai.coworkers.core.model.workflow.step.slot.loop.LoopStepSlotAssembler;
import ai.coworkers.core.model.workflow.step.slot.parallel.ParallelStepSlot;
import ai.coworkers.core.model.workflow.step.slot.parallel.ParallelStepSlotAssembler;
import ai.coworkers.core.model.workflow.step.slot.task.TaskStepSlot;
import ai.coworkers.core.model.workflow.step.slot.task.TaskStepSlotAssembler;
import ai.coworkers.core.model.workflow.step.template.BranchStepTemplate;
import ai.coworkers.core.model.workflow.step.template.LoopStepTemplate;
import ai.coworkers.core.model.workflow.step.template.ParallelStepTemplate;
import ai.coworkers.core.model.workflow.step.template.StepTemplate;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import ai.coworkers.core.model.workflow.step.template.task.TaskStepTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("StepSlotFactory")
class StepSlotFactoryTest {

  private static final StepOutput RESULT = new StepOutput() {};
  private static final List<StepSlotAssembler<?>> CORE_ASSEMBLERS =
      List.of(
          new TaskStepSlotAssembler(),
          new BranchStepSlotAssembler(),
          new LoopStepSlotAssembler(),
          new ParallelStepSlotAssembler());

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

  private record ReviewTemplate(StepTemplateId id, @Nullable StepTemplateId next)
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

  private record ReviewSlot(StepSlotId id, StepTemplate template) implements StepSlot {
    @Override
    public StepOutput process(StepInput input) {
      return RESULT;
    }
  }

  private interface FirstMarker extends StepTemplate {}

  private interface SecondMarker extends StepTemplate {}

  private record AmbiguousTemplate(StepTemplateId id) implements FirstMarker, SecondMarker {
    @Override
    public StepType stepType() {
      return DefaultStepType.TASK;
    }

    @Override
    public boolean isTaskStep() {
      return false;
    }

    @Override
    public Long version() {
      return 1L;
    }
  }

  private static StepTemplateId templateId() {
    return new StepTemplateId(UUID.randomUUID());
  }

  private static <T extends StepTemplate> StepSlotAssembler<T> unusedAssembler(Class<T> type) {
    return new StepSlotAssembler<>() {
      @Override
      public Class<T> templateType() {
        return type;
      }

      @Override
      public StepSlot assemble(
          T template, StepSlotId id, Function<StepTemplateId, StepSlotId> slotIdOf) {
        throw new AssertionError("assemble must not be called");
      }
    };
  }

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @Test
    @DisplayName("같은 템플릿 타입의 전략이 둘 이상이면 거부함")
    void rejectsDuplicateTemplateType() {
      List<StepSlotAssembler<?>> assemblers =
          List.of(new TaskStepSlotAssembler(), new TaskStepSlotAssembler());

      assertThatThrownBy(() -> new StepSlotFactory(assemblers))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("duplicate assembler template type: " + TaskStepTemplate.class.getName());
    }
  }

  @Nested
  @DisplayName("create")
  class Create {

    private final StepSlotFactory factory = new StepSlotFactory(CORE_ASSEMBLERS);

    @Test
    @DisplayName("템플릿마다 슬롯을 만들고 출구를 대응하는 슬롯 식별자로 이음")
    void wiresExitsToSlotIds() {
      StepTemplateId start = templateId();
      StepTemplateId branch = templateId();
      StepTemplateId satisfied = templateId();
      StepTemplateId loop = templateId();
      StepTemplateId parallel = templateId();
      StepTemplateId first = templateId();
      StepTemplateId second = templateId();
      StepTemplateId end = templateId();
      TaskStepTemplate startTemplate = new TestTaskTemplate(start, branch);
      BranchStepTemplate branchTemplate =
          new BranchStepTemplate(branch, 1L, FixedStepConditionType.leaf(true), satisfied, loop);
      TaskStepTemplate satisfiedTemplate = new TestTaskTemplate(satisfied, parallel);
      LoopStepTemplate loopTemplate =
          new LoopStepTemplate(loop, 1L, FixedStepConditionType.leaf(true), start, end);
      ParallelStepTemplate parallelTemplate =
          new ParallelStepTemplate(parallel, 1L, List.of(first, second), end);
      TaskStepTemplate firstTemplate = new TestTaskTemplate(first, null);
      TaskStepTemplate secondTemplate = new TestTaskTemplate(second, null);
      TaskStepTemplate endTemplate = new TestTaskTemplate(end, null);

      Map<StepTemplateId, StepSlot> slots =
          factory.create(
              List.of(
                  startTemplate,
                  branchTemplate,
                  satisfiedTemplate,
                  loopTemplate,
                  parallelTemplate,
                  firstTemplate,
                  secondTemplate,
                  endTemplate));
      Function<StepTemplateId, StepSlotId> slotIdOf = templateId -> slots.get(templateId).id();

      assertThat(slots)
          .containsOnlyKeys(start, branch, satisfied, loop, parallel, first, second, end)
          .containsEntry(
              start, new TaskStepSlot(slotIdOf.apply(start), startTemplate, slotIdOf.apply(branch)))
          .containsEntry(
              branch,
              new BranchStepSlot(
                  slotIdOf.apply(branch),
                  branchTemplate,
                  slotIdOf.apply(satisfied),
                  slotIdOf.apply(loop)))
          .containsEntry(
              loop,
              new LoopStepSlot(
                  slotIdOf.apply(loop), loopTemplate, slotIdOf.apply(start), slotIdOf.apply(end)))
          .containsEntry(
              parallel,
              new ParallelStepSlot(
                  slotIdOf.apply(parallel),
                  parallelTemplate,
                  List.of(slotIdOf.apply(first), slotIdOf.apply(second)),
                  slotIdOf.apply(end)))
          .containsEntry(end, new TaskStepSlot(slotIdOf.apply(end), endTemplate, null));
    }

    @Test
    @DisplayName("슬롯마다 서로 다른 식별자를 발행함")
    void issuesDistinctSlotIds() {
      List<StepTemplate> templates = new ArrayList<>();
      for (int i = 0; i < 100; i++) {
        templates.add(new TestTaskTemplate(templateId(), null));
      }

      assertThat(factory.create(templates).values())
          .extracting(StepSlot::id)
          .doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("하위 타입 전략이 있으면 그 전략으로 조립함")
    void prefersMostSpecificAssembler() {
      StepSlotAssembler<ReviewTemplate> reviewAssembler =
          new StepSlotAssembler<>() {
            @Override
            public Class<ReviewTemplate> templateType() {
              return ReviewTemplate.class;
            }

            @Override
            public StepSlot assemble(
                ReviewTemplate template,
                StepSlotId id,
                Function<StepTemplateId, StepSlotId> slotIdOf) {
              return new ReviewSlot(id, template);
            }
          };
      List<StepSlotAssembler<?>> assemblers = new ArrayList<>(CORE_ASSEMBLERS);
      assemblers.add(reviewAssembler);
      StepTemplateId review = templateId();
      StepTemplateId task = templateId();

      Map<StepTemplateId, StepSlot> slots =
          new StepSlotFactory(assemblers)
              .create(List.of(new ReviewTemplate(review, task), new TestTaskTemplate(task, null)));

      assertThat(slots.get(review)).isInstanceOf(ReviewSlot.class);
      assertThat(slots.get(task)).isInstanceOf(TaskStepSlot.class);
    }

    @Test
    @DisplayName("템플릿 식별자가 중복되면 거부함")
    void rejectsDuplicateTemplateId() {
      StepTemplateId duplicate = templateId();
      List<StepTemplate> templates =
          List.of(new TestTaskTemplate(duplicate, null), new TestTaskTemplate(duplicate, null));

      assertThatThrownBy(() -> factory.create(templates))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("duplicate template id: " + duplicate);
    }

    @Test
    @DisplayName("틀에 없는 템플릿을 가리키면 거부함")
    void rejectsUnknownTemplateReference() {
      StepTemplateId missing = templateId();
      List<StepTemplate> templates = List.of(new TestTaskTemplate(templateId(), missing));

      assertThatThrownBy(() -> factory.create(templates))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("unknown template id: " + missing);
    }

    @Test
    @DisplayName("맞는 전략이 없으면 거부함")
    void rejectsTemplateWithoutAssembler() {
      StepSlotFactory branchOnly = new StepSlotFactory(List.of(new BranchStepSlotAssembler()));
      List<StepTemplate> templates = List.of(new TestTaskTemplate(templateId(), null));

      assertThatThrownBy(() -> branchOnly.create(templates))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("no single assembler for template: " + TestTaskTemplate.class.getName());
    }

    @Test
    @DisplayName("가장 구체적인 전략이 둘 이상이면 거부함")
    void rejectsAmbiguousAssemblers() {
      StepSlotFactory ambiguous =
          new StepSlotFactory(
              List.of(unusedAssembler(FirstMarker.class), unusedAssembler(SecondMarker.class)));
      List<StepTemplate> templates = List.of(new AmbiguousTemplate(templateId()));

      assertThatThrownBy(() -> ambiguous.create(templates))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("no single assembler for template: " + AmbiguousTemplate.class.getName());
    }
  }
}
