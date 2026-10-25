package ai.coworkers.core.model.runtime.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.runtime.saga.Saga;
import ai.coworkers.core.model.runtime.saga.SagaCodec;
import ai.coworkers.core.model.runtime.saga.SagaData;
import ai.coworkers.core.model.runtime.saga.SagaState;
import ai.coworkers.core.model.runtime.step.slot.StepSlotCodec;
import ai.coworkers.core.model.runtime.step.slot.StepSlotFactory;
import ai.coworkers.core.model.runtime.step.slot.branch.BranchStepSlotAssembler;
import ai.coworkers.core.model.runtime.step.slot.parallel.ParallelStepSlotAssembler;
import ai.coworkers.core.model.template.step.BranchStepTemplate;
import ai.coworkers.core.model.template.step.ParallelStepTemplate;
import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.condition.FixedStepConditionType;
import ai.coworkers.core.model.template.step.input.StepInput;
import ai.coworkers.core.model.template.workflow.WorkflowTemplate;
import ai.coworkers.core.model.template.workflow.WorkflowTemplateId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("WorkflowCodec")
class WorkflowCodecTest {

  private static final WorkflowCodec CODEC =
      new WorkflowCodec(new StepSlotCodec(), new SagaCodec());

  private final StepTemplateId parallel = StepTemplateId.generate();
  private final StepTemplateId first = StepTemplateId.generate();
  private final StepTemplateId second = StepTemplateId.generate();
  private final StepTemplateId join = StepTemplateId.generate();
  private final List<StepTemplate> templates =
      List.of(
          new ParallelStepTemplate(parallel, 1L, List.of(first, second), join),
          new BranchStepTemplate(first, 1L, FixedStepConditionType.leaf(true), join, join),
          new BranchStepTemplate(second, 1L, FixedStepConditionType.leaf(true), join, join),
          new ParallelStepTemplate(join, 1L, List.of(), parallel));
  private final Map<StepTemplateId, StepTemplate> byId =
      templates.stream().collect(Collectors.toMap(StepTemplate::id, Function.identity()));
  private final BiFunction<StepTemplateId, Long, StepTemplate> snapshotOf =
      (id, version) -> byId.get(id);

  /** 병렬로 분기한 뒤 첫째 갈래만 합류 슬롯에 도착한 실행 */
  private Workflow partiallyJoined() {
    List<StepTemplateId> ids = templates.stream().map(StepTemplate::id).toList();
    Workflow started =
        Workflow.start(
            new WorkflowTemplate(WorkflowTemplateId.generate(), 2L, parallel, ids),
            new StepSlotFactory(
                    List.of(new BranchStepSlotAssembler(), new ParallelStepSlotAssembler()))
                .create(templates));
    Workflow forked = run(started, parallel);
    return run(forked, first);
  }

  private static Workflow run(Workflow workflow, StepTemplateId template) {
    Saga saga =
        workflow.sagas().values().stream()
            .filter(candidate -> candidate.state() == SagaState.RUNNING)
            .filter(
                candidate ->
                    Objects.requireNonNull(candidate.activeNode())
                        .stepSlot()
                        .template()
                        .id()
                        .equals(template))
            .findFirst()
            .orElseThrow();
    return workflow.advance(
        saga.id(), Objects.requireNonNull(saga.activeNode()).run(StepInput.empty()));
  }

  @Nested
  @DisplayName("encode")
  class Encode {

    @Test
    @DisplayName("식별 값, 틀 식별 값·버전과 슬롯·사가·합류 대기의 저장 형태를 담음")
    void encodesFields() {
      Workflow workflow = partiallyJoined();

      WorkflowData data = CODEC.encode(workflow);

      assertThat(data)
          .returns(workflow.id().value(), WorkflowData::id)
          .returns(workflow.templateId().value(), WorkflowData::templateId)
          .returns(2L, WorkflowData::templateVersion);
      assertThat(data.slots()).hasSize(4);
      assertThat(data.sagas())
          .extracting(SagaData::state)
          .containsExactlyInAnyOrder("WAITING", "JOINED", "RUNNING");
      assertThat(data.joins())
          .singleElement()
          .satisfies(join -> assertThat(join.joined()).hasSize(1));
    }
  }

  @Nested
  @DisplayName("decode")
  class Decode {

    @Test
    @DisplayName("저장 형태와 템플릿 스냅샷으로 같은 실행을 복원함")
    void decodesEncoded() {
      Workflow workflow = partiallyJoined();

      assertThat(CODEC.decode(CODEC.encode(workflow), snapshotOf)).isEqualTo(workflow);
    }

    @Test
    @DisplayName("복원한 실행을 이어서 진행할 수 있음")
    void continuesAfterDecode() {
      Workflow restored = CODEC.decode(CODEC.encode(partiallyJoined()), snapshotOf);

      assertThat(run(restored, second).joins()).isEmpty();
    }

    @Test
    @DisplayName("사가가 없는 슬롯을 가리키면 거부함")
    void rejectsUnknownSlot() {
      WorkflowData data = CODEC.encode(partiallyJoined());
      UUID missing = UUID.randomUUID();
      SagaData dangling =
          new SagaData(
              UUID.randomUUID(),
              "RUNNING",
              new SagaData.NodeData(UUID.randomUUID(), "ARRIVED", missing));
      WorkflowData broken =
          new WorkflowData(
              data.id(),
              data.templateId(),
              data.templateVersion(),
              data.slots(),
              List.of(dangling),
              List.of());

      assertThatThrownBy(() -> CODEC.decode(broken, snapshotOf))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageEndingWith("unknown slot id: StepSlotId[value=" + missing + "]");
    }
  }
}
