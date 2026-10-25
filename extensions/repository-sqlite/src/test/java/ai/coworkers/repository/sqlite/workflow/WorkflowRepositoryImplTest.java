package ai.coworkers.repository.sqlite.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.runtime.saga.Saga;
import ai.coworkers.core.model.runtime.saga.SagaCodec;
import ai.coworkers.core.model.runtime.saga.SagaState;
import ai.coworkers.core.model.runtime.step.output.StepOutput;
import ai.coworkers.core.model.runtime.step.slot.StepSlotCodec;
import ai.coworkers.core.model.runtime.step.slot.StepSlotFactory;
import ai.coworkers.core.model.runtime.step.slot.parallel.ParallelStepSlotAssembler;
import ai.coworkers.core.model.runtime.step.slot.task.TaskStepSlotAssembler;
import ai.coworkers.core.model.runtime.workflow.Workflow;
import ai.coworkers.core.model.runtime.workflow.WorkflowCodec;
import ai.coworkers.core.model.runtime.workflow.WorkflowId;
import ai.coworkers.core.model.template.step.ParallelStepTemplate;
import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.input.StepInput;
import ai.coworkers.core.model.template.step.log.StepTemplateLog;
import ai.coworkers.core.model.template.step.log.StepTemplateLogRepository;
import ai.coworkers.core.model.template.step.task.TaskStepTemplate;
import ai.coworkers.core.model.template.workflow.WorkflowTemplate;
import ai.coworkers.core.model.template.workflow.WorkflowTemplateId;
import ai.coworkers.repository.sqlite.InMemorySqlite;
import ai.coworkers.serd.json.workflow.WorkflowJson;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("WorkflowRepositoryImpl")
class WorkflowRepositoryImplTest {

  private static final StepOutput RESULT = new StepOutput() {};
  private static final StepSlotFactory FACTORY =
      new StepSlotFactory(
          List.of(
              new TaskStepSlotAssembler((template, input) -> RESULT),
              new ParallelStepSlotAssembler()));

  private record TestTaskTemplate(StepTemplateId id, @Nullable StepTemplateId next)
      implements TaskStepTemplate {
    @Override
    public Long version() {
      return 1L;
    }
  }

  private final List<StepTemplate> snapshots = new ArrayList<>();
  private final StepTemplateLogRepository templateLogs =
      new StepTemplateLogRepository() {
        @Override
        public void append(StepTemplateLog log) {
          snapshots.add(log.snapshot());
        }

        @Override
        public Optional<StepTemplate> findSnapshot(StepTemplateId id, Long version) {
          return snapshots.stream()
              .filter(snapshot -> snapshot.id().equals(id) && snapshot.version().equals(version))
              .findFirst();
        }
      };
  private final WorkflowRepositoryImpl repository =
      new WorkflowRepositoryImpl(
          InMemorySqlite.migrated(),
          new WorkflowCodec(new StepSlotCodec(), new SagaCodec()),
          new WorkflowJson(),
          templateLogs);

  private final StepTemplateId parallel = StepTemplateId.generate();
  private final StepTemplateId branch = StepTemplateId.generate();
  private final StepTemplateId join = StepTemplateId.generate();

  private static Workflow start(List<StepTemplate> templates) {
    List<StepTemplateId> ids = templates.stream().map(StepTemplate::id).toList();
    return Workflow.start(
        new WorkflowTemplate(WorkflowTemplateId.generate(), 1L, ids.getFirst(), ids),
        FACTORY.create(templates));
  }

  /** 병렬 → 갈래 → 합류로 이어지는 실행. 템플릿 스냅샷을 로그에 남김. */
  private Workflow parallelWorkflow() {
    List<StepTemplate> templates =
        List.of(
            new ParallelStepTemplate(parallel, 2L, List.of(branch), join),
            new ParallelStepTemplate(branch, 3L, List.of(), join),
            new ParallelStepTemplate(join, 4L, List.of(), parallel));
    snapshots.addAll(templates);
    return start(templates);
  }

  private static Workflow runFirstRunning(Workflow workflow) {
    Saga saga =
        workflow.sagas().values().stream()
            .filter(candidate -> candidate.state() == SagaState.RUNNING)
            .findFirst()
            .orElseThrow();
    return workflow.advance(
        saga.id(), Objects.requireNonNull(saga.activeNode()).run(StepInput.empty()));
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("저장한 실행을 기록된 버전의 템플릿 스냅샷으로 복원해 반환함")
    void returnsSavedWorkflow() {
      Workflow forked = runFirstRunning(parallelWorkflow());
      repository.save(forked);

      assertThat(repository.findById(forked.id())).contains(forked);
    }

    @Test
    @DisplayName("실행이 없으면 빈 값을 반환함")
    void returnsEmptyForUnknownId() {
      assertThat(repository.findById(WorkflowId.generate())).isEmpty();
    }

    @Test
    @DisplayName("기록된 버전의 템플릿 스냅샷이 없으면 거부함")
    void rejectsMissingSnapshot() {
      Workflow workflow = parallelWorkflow();
      repository.save(workflow);
      snapshots.clear();

      assertThatThrownBy(() -> repository.findById(workflow.id()))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessageStartingWith("unknown template snapshot");
    }

    @Test
    @DisplayName("작업 슬롯이 든 실행은 복원을 거부함")
    void rejectsTaskSlot() {
      Workflow workflow = start(List.of(new TestTaskTemplate(StepTemplateId.generate(), null)));
      repository.save(workflow);

      assertThatThrownBy(() -> repository.findById(workflow.id()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageStartingWith("task slot restore unsupported");
    }
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("같은 식별자의 실행이 있으면 진행한 실행으로 갱신함")
    void updatesExisting() {
      Workflow started = parallelWorkflow();
      repository.save(started);
      Workflow forked = runFirstRunning(started);

      repository.save(forked);

      assertThat(repository.findById(started.id())).contains(forked);
    }
  }
}
