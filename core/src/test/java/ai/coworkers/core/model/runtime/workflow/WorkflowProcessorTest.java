package ai.coworkers.core.model.runtime.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.runtime.saga.Saga;
import ai.coworkers.core.model.runtime.saga.SagaId;
import ai.coworkers.core.model.runtime.saga.SagaLog;
import ai.coworkers.core.model.runtime.saga.SagaState;
import ai.coworkers.core.model.runtime.step.output.StepOutput;
import ai.coworkers.core.model.runtime.step.slot.StepSlotFactory;
import ai.coworkers.core.model.runtime.step.slot.parallel.ParallelStepSlotAssembler;
import ai.coworkers.core.model.runtime.step.slot.task.TaskExecutor;
import ai.coworkers.core.model.runtime.step.slot.task.TaskStepSlotAssembler;
import ai.coworkers.core.model.template.step.ParallelStepTemplate;
import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.input.StepInput;
import ai.coworkers.core.model.template.step.task.TaskStepTemplate;
import ai.coworkers.core.model.template.workflow.WorkflowTemplate;
import ai.coworkers.core.model.template.workflow.WorkflowTemplateId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("WorkflowProcessor")
class WorkflowProcessorTest {

  private static final StepOutput RESULT = new StepOutput() {};
  private static final TaskExecutor EXECUTOR = (template, input) -> RESULT;

  private record TestTaskTemplate(StepTemplateId id, Long version, @Nullable StepTemplateId next)
      implements TaskStepTemplate {}

  private final StepTemplateId start = StepTemplateId.generate();
  private final StepTemplateId first = StepTemplateId.generate();
  private final StepTemplateId second = StepTemplateId.generate();
  private final StepTemplateId join = StepTemplateId.generate();
  private final WorkflowTemplate template =
      new WorkflowTemplate(
          WorkflowTemplateId.generate(), 2L, start, List.of(start, first, second, join));

  private final Map<WorkflowTemplateId, WorkflowTemplate> templates =
      new HashMap<>(Map.of(template.id(), template));
  private final Map<StepTemplateId, StepTemplate> steps =
      new HashMap<>(
          Map.of(
              start, new ParallelStepTemplate(start, 5L, List.of(first, second), join),
              first, new TestTaskTemplate(first, 1L, join),
              second, new TestTaskTemplate(second, 1L, join),
              join, new TestTaskTemplate(join, 1L, null)));
  private final Map<WorkflowId, Workflow> workflows = new HashMap<>();
  private final List<SagaLog> logs = new ArrayList<>();
  private final WorkflowProcessor processor =
      new WorkflowProcessor(
          id -> Optional.ofNullable(templates.get(id)),
          id -> Optional.ofNullable(steps.get(id)),
          new StepSlotFactory(
              List.of(new TaskStepSlotAssembler(EXECUTOR), new ParallelStepSlotAssembler())),
          id -> Optional.ofNullable(workflows.get(id)),
          workflow -> workflows.put(workflow.id(), workflow),
          logs::add);

  private List<Saga> snapshots() {
    return logs.stream().map(SagaLog::snapshot).toList();
  }

  @Nested
  @DisplayName("start")
  class Start {

    @Test
    @DisplayName("틀 현재본의 버전과 템플릿 현재본으로 실행을 시작해 저장하고 첫 사가 로그를 추가함")
    void startsWithCurrentSnapshot() {
      Workflow started = processor.start(template.id());

      assertThat(started)
          .returns(template.id(), Workflow::templateId)
          .returns(2L, Workflow::templateVersion);
      assertThat(started.slots().values())
          .extracting(slot -> slot.template())
          .containsExactlyInAnyOrderElementsOf(steps.values());
      assertThat(workflows).containsExactly(Map.entry(started.id(), started));
      assertThat(snapshots()).containsExactlyElementsOf(started.sagas().values());
    }

    @Test
    @DisplayName("시작한 뒤 템플릿을 고쳐도 실행의 슬롯은 시작 시점의 템플릿을 유지함")
    void keepsTemplatesOfStartTime() {
      Workflow started = processor.start(template.id());
      StepTemplate original = steps.get(join);

      steps.put(join, new TestTaskTemplate(join, 2L, null));

      assertThat(processor.findById(started.id()).orElseThrow().slots().values())
          .extracting(slot -> slot.template())
          .contains(original)
          .doesNotContain(steps.get(join));
    }

    @Test
    @DisplayName("워크플로우 틀이 없으면 거부함")
    void rejectsUnknownWorkflowTemplate() {
      WorkflowTemplateId unknown = WorkflowTemplateId.generate();

      assertThatThrownBy(() -> processor.start(unknown))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessage("unknown workflow template id: " + unknown);
    }

    @Test
    @DisplayName("틀에 속한 템플릿이 없으면 거부함")
    void rejectsUnknownStepTemplate() {
      steps.remove(second);

      assertThatThrownBy(() -> processor.start(template.id()))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessage("unknown template id: " + second);
      assertThat(workflows).isEmpty();
    }
  }

  @Nested
  @DisplayName("advance")
  class Advance {

    @Test
    @DisplayName("진행한 실행을 저장하고 바뀐 사가의 로그만 추가함")
    void storesAndLogsChangedSagas() {
      Workflow started = processor.start(template.id());
      Workflow forked = run(started, started.sagas().values().iterator().next());
      Saga branch =
          forked.sagas().values().stream()
              .filter(saga -> saga.state() == SagaState.RUNNING)
              .findFirst()
              .orElseThrow();
      logs.clear();

      Workflow advanced = run(forked, branch);

      assertThat(workflows).containsExactly(Map.entry(advanced.id(), advanced));
      assertThat(snapshots()).containsExactly(advanced.sagas().get(branch.id()));
    }

    @Test
    @DisplayName("실행이 없으면 거부함")
    void rejectsUnknownWorkflow() {
      WorkflowId unknown = WorkflowId.generate();

      assertThatThrownBy(() -> processor.advance(unknown, SagaId.generate(), RESULT))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessage("unknown workflow id: " + unknown);
    }
  }

  /** 사가의 현재 노드를 실행한 출력으로 진행함. */
  private Workflow run(Workflow workflow, Saga saga) {
    StepOutput output = Objects.requireNonNull(saga.activeNode()).run(StepInput.empty());
    return processor.advance(workflow.id(), saga.id(), output);
  }
}
