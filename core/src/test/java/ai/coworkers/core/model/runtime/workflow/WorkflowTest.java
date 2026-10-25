package ai.coworkers.core.model.runtime.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.runtime.saga.Saga;
import ai.coworkers.core.model.runtime.saga.SagaId;
import ai.coworkers.core.model.runtime.saga.SagaState;
import ai.coworkers.core.model.runtime.step.node.StepNode;
import ai.coworkers.core.model.runtime.step.output.StepOutput;
import ai.coworkers.core.model.runtime.step.slot.StepSlot;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Workflow")
class WorkflowTest {

  private static final StepOutput RESULT = new StepOutput() {};
  private static final TaskExecutor EXECUTOR = (template, input) -> RESULT;
  private static final StepSlotFactory FACTORY =
      new StepSlotFactory(
          List.of(new TaskStepSlotAssembler(EXECUTOR), new ParallelStepSlotAssembler()));
  private static final Long TEMPLATE_VERSION = 3L;

  private record TestTaskTemplate(StepTemplateId id, @Nullable StepTemplateId next)
      implements TaskStepTemplate {
    @Override
    public Long version() {
      return 1L;
    }
  }

  private final StepTemplateId start = StepTemplateId.generate();
  private final StepTemplateId parallel = StepTemplateId.generate();
  private final StepTemplateId first = StepTemplateId.generate();
  private final StepTemplateId second = StepTemplateId.generate();
  private final StepTemplateId join = StepTemplateId.generate();

  private static WorkflowTemplate templateOf(StepTemplate... steps) {
    List<StepTemplateId> ids = Arrays.stream(steps).map(StepTemplate::id).toList();
    return new WorkflowTemplate(
        WorkflowTemplateId.generate(), TEMPLATE_VERSION, ids.getFirst(), ids);
  }

  private static Workflow startOf(StepTemplate... steps) {
    return Workflow.start(templateOf(steps), FACTORY.create(List.of(steps)));
  }

  /** 시작 → 병렬[첫째, 둘째] → 합류 */
  private Workflow forkingWorkflow() {
    return startOf(
        new TestTaskTemplate(start, parallel),
        new ParallelStepTemplate(parallel, 1L, List.of(first, second), join),
        new TestTaskTemplate(first, join),
        new TestTaskTemplate(second, join),
        new TestTaskTemplate(join, null));
  }

  /** 템플릿의 슬롯을 가리키는 사가를 찾음. */
  private static Saga sagaAt(Workflow workflow, StepTemplateId template) {
    return workflow.sagas().values().stream()
        .filter(saga -> saga.activeNode() != null)
        .filter(saga -> slotTemplateOf(saga).equals(template))
        .findFirst()
        .orElseThrow();
  }

  private static StepTemplateId slotTemplateOf(Saga saga) {
    return Objects.requireNonNull(saga.activeNode()).stepSlot().template().id();
  }

  /** 사가의 현재 노드를 실행한 출력으로 진행함. */
  private static Workflow step(Workflow workflow, StepTemplateId template) {
    Saga saga = sagaAt(workflow, template);
    StepNode node = Objects.requireNonNull(saga.activeNode());
    return workflow.advance(saga.id(), node.run(StepInput.empty()));
  }

  @Nested
  @DisplayName("start")
  class Start {

    @Test
    @DisplayName("틀 식별자와 버전을 기록하고 시작 슬롯에서 진행 상태의 첫 사가를 만듦")
    void startsFirstSagaAtStartSlot() {
      TestTaskTemplate task = new TestTaskTemplate(start, null);
      WorkflowTemplate template = templateOf(task);
      Map<StepTemplateId, StepSlot> stamped = FACTORY.create(List.of(task));

      Workflow workflow = Workflow.start(template, stamped);

      assertThat(workflow)
          .returns(template.id(), Workflow::templateId)
          .returns(TEMPLATE_VERSION, Workflow::templateVersion)
          .returns(Map.of(stamped.get(start).id(), stamped.get(start)), Workflow::slots)
          .returns(List.of(), Workflow::joins);
      assertThat(workflow.sagas().values())
          .singleElement()
          .returns(SagaState.RUNNING, Saga::state)
          .returns(stamped.get(start), saga -> saga.activeNode().stepSlot());
    }

    @Test
    @DisplayName("시작 템플릿의 슬롯이 없으면 거부함")
    void rejectsMissingStartSlot() {
      WorkflowTemplate template = templateOf(new TestTaskTemplate(start, null));

      assertThatThrownBy(() -> Workflow.start(template, Map.of()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("start slot not stamped: " + start);
    }
  }

  @Nested
  @DisplayName("advance")
  class Advance {

    @Test
    @DisplayName("작업 출력이면 같은 사가를 다음 슬롯의 새 노드로 옮김")
    void movesToNextSlot() {
      Workflow workflow =
          startOf(new TestTaskTemplate(start, join), new TestTaskTemplate(join, null));
      SagaId sagaId = sagaAt(workflow, start).id();

      Workflow advanced = step(workflow, start);

      assertThat(sagaAt(advanced, join))
          .returns(sagaId, Saga::id)
          .returns(SagaState.RUNNING, Saga::state);
    }

    @Test
    @DisplayName("작업 출력에 다음 슬롯이 없으면 사가를 끝냄")
    void endsWithoutNextSlot() {
      Workflow workflow = startOf(new TestTaskTemplate(start, null));

      Workflow advanced = step(workflow, start);

      assertThat(advanced.sagas().values()).singleElement().returns(SagaState.ENDED, Saga::state);
    }

    @Test
    @DisplayName("병렬 출력이면 사가를 대기시키고 갈래마다 사가를 만들어 합류 대기를 등록함")
    void forksBranchSagas() {
      Workflow forked = step(step(forkingWorkflow(), start), parallel);
      Saga origin = sagaAt(forked, parallel);
      Saga firstBranch = sagaAt(forked, first);
      Saga secondBranch = sagaAt(forked, second);

      assertThat(origin.state()).isSameAs(SagaState.WAITING);
      assertThat(List.of(firstBranch, secondBranch))
          .extracting(Saga::state)
          .containsOnly(SagaState.RUNNING);
      assertThat(forked.joins())
          .singleElement()
          .returns(origin.id(), SagaJoin::origin)
          .returns(List.of(firstBranch.id(), secondBranch.id()), SagaJoin::branches)
          .returns(Set.of(), SagaJoin::joined);
    }

    @Test
    @DisplayName("일부 갈래만 합류 슬롯에 도착하면 대기한 사가는 계속 기다림")
    void keepsWaitingUntilAllBranchesArrive() {
      Workflow forked = step(step(forkingWorkflow(), start), parallel);
      SagaId firstBranch = sagaAt(forked, first).id();

      Workflow arrived = step(forked, first);

      assertThat(arrived.sagas().get(firstBranch).state()).isSameAs(SagaState.JOINED);
      assertThat(sagaAt(arrived, parallel).state()).isSameAs(SagaState.WAITING);
      assertThat(arrived.joins()).singleElement().returns(Set.of(firstBranch), SagaJoin::joined);
    }

    @Test
    @DisplayName("갈래가 모두 합류 슬롯에 도착하면 대기한 사가가 합류 슬롯으로 옮겨감")
    void resumesOriginAtJoin() {
      Workflow forked = step(step(forkingWorkflow(), start), parallel);
      SagaId origin = sagaAt(forked, parallel).id();

      Workflow joined = step(step(forked, first), second);

      assertThat(sagaAt(joined, join))
          .returns(origin, Saga::id)
          .returns(SagaState.RUNNING, Saga::state);
      assertThat(joined.joins()).isEmpty();
      assertThat(joined.sagas().values())
          .extracting(Saga::state)
          .containsExactlyInAnyOrder(SagaState.RUNNING, SagaState.JOINED, SagaState.JOINED);
    }

    @Test
    @DisplayName("안쪽 병렬이 합류한 뒤 바깥 병렬의 합류 슬롯에 도착하면 바깥 사가가 이어감")
    void joinsNestedForks() {
      StepTemplateId inner = StepTemplateId.generate();
      StepTemplateId innerFirst = StepTemplateId.generate();
      StepTemplateId innerJoin = StepTemplateId.generate();
      Workflow workflow =
          startOf(
              new ParallelStepTemplate(parallel, 1L, List.of(inner, second), join),
              new ParallelStepTemplate(inner, 1L, List.of(innerFirst), innerJoin),
              new TestTaskTemplate(innerFirst, innerJoin),
              new TestTaskTemplate(innerJoin, join),
              new TestTaskTemplate(second, join),
              new TestTaskTemplate(join, null));
      SagaId outer = sagaAt(workflow, parallel).id();

      Workflow innerForked = step(step(workflow, parallel), inner);
      Workflow innerJoined = step(innerForked, innerFirst);
      Workflow outerJoined = step(step(innerJoined, innerJoin), second);

      assertThat(sagaAt(innerJoined, innerJoin).id()).isEqualTo(sagaAt(innerForked, inner).id());
      assertThat(sagaAt(outerJoined, join).id()).isEqualTo(outer);
      assertThat(outerJoined.joins()).isEmpty();
    }

    @Test
    @DisplayName("갈래가 없으면 대기 없이 합류 슬롯으로 옮겨감")
    void joinsImmediatelyWithoutBranches() {
      Workflow workflow =
          startOf(
              new ParallelStepTemplate(parallel, 1L, List.of(), join),
              new TestTaskTemplate(join, null));
      SagaId origin = sagaAt(workflow, parallel).id();

      Workflow advanced = step(workflow, parallel);

      assertThat(sagaAt(advanced, join))
          .returns(origin, Saga::id)
          .returns(SagaState.RUNNING, Saga::state);
      assertThat(advanced.joins()).isEmpty();
    }

    @Test
    @DisplayName("갈래 사가가 합류 슬롯에 닿기 전에 끝나면 거부함")
    void rejectsBranchEndingBeforeJoin() {
      Workflow forked =
          step(
              startOf(
                  new ParallelStepTemplate(parallel, 1L, List.of(first), join),
                  new TestTaskTemplate(first, null),
                  new TestTaskTemplate(join, null)),
              parallel);
      SagaId branch = sagaAt(forked, first).id();

      assertThatThrownBy(() -> step(forked, first))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("branch saga ended before join: " + branch);
    }

    @Test
    @DisplayName("사가가 없으면 거부함")
    void rejectsUnknownSaga() {
      SagaId unknown = SagaId.generate();

      assertThatThrownBy(() -> forkingWorkflow().advance(unknown, RESULT))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessage("unknown saga id: " + unknown);
    }

    @Test
    @DisplayName("지원하지 않는 출력이면 거부함")
    void rejectsUnsupportedOutput() {
      Workflow workflow = forkingWorkflow();

      assertThatThrownBy(() -> workflow.advance(sagaAt(workflow, start).id(), RESULT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("unsupported step output: " + RESULT.getClass().getName());
    }
  }

  @Nested
  @DisplayName("isFinished")
  class IsFinished {

    @Test
    @DisplayName("끝나지 않은 사가가 있으면 거짓을 반환함")
    void falseWhileSagaRemains() {
      assertThat(step(step(forkingWorkflow(), start), parallel).isFinished()).isFalse();
    }

    @Test
    @DisplayName("모든 사가가 합류하거나 끝나면 참을 반환함")
    void trueWhenAllSagasFinished() {
      Workflow forked = step(step(forkingWorkflow(), start), parallel);

      assertThat(step(step(step(forked, first), second), join).isFinished()).isTrue();
    }
  }
}
