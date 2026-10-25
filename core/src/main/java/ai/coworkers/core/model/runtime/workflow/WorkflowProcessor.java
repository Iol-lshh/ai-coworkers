package ai.coworkers.core.model.runtime.workflow;

import ai.coworkers.core.common.UuidV7;
import ai.coworkers.core.model.runtime.saga.Saga;
import ai.coworkers.core.model.runtime.saga.SagaId;
import ai.coworkers.core.model.runtime.saga.SagaLog;
import ai.coworkers.core.model.runtime.step.output.StepOutput;
import ai.coworkers.core.model.runtime.step.slot.StepSlotFactory;
import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.workflow.WorkflowTemplate;
import ai.coworkers.core.model.template.workflow.WorkflowTemplateId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/** 워크플로우 실행의 시작·진행 흐름을 맡는 도메인 서비스. 실행을 저장할 때마다 바뀐 사가의 로그를 추가함. */
public final class WorkflowProcessor {

  private final Function<WorkflowTemplateId, Optional<WorkflowTemplate>> findTemplate;
  private final Function<StepTemplateId, Optional<StepTemplate>> findStepTemplate;
  private final StepSlotFactory slotFactory;
  private final Function<WorkflowId, Optional<Workflow>> findById;
  private final Consumer<Workflow> save;
  private final Consumer<SagaLog> append;

  /**
   * 저장소 동작과 슬롯 팩토리를 받음.
   *
   * @param findTemplate 식별자로 워크플로우 틀 현재본을 찾는 동작
   * @param findStepTemplate 식별자로 템플릿 현재본을 찾는 동작
   * @param slotFactory 틀의 템플릿을 슬롯으로 찍어내는 팩토리
   * @param findById 식별자로 실행을 찾는 동작
   * @param save 실행을 저장하는 동작. 같은 식별자의 실행이 있으면 갱신함
   * @param append 사가 로그를 추가하는 동작
   */
  public WorkflowProcessor(
      Function<WorkflowTemplateId, Optional<WorkflowTemplate>> findTemplate,
      Function<StepTemplateId, Optional<StepTemplate>> findStepTemplate,
      StepSlotFactory slotFactory,
      Function<WorkflowId, Optional<Workflow>> findById,
      Consumer<Workflow> save,
      Consumer<SagaLog> append) {
    this.findTemplate = findTemplate;
    this.findStepTemplate = findStepTemplate;
    this.slotFactory = slotFactory;
    this.findById = findById;
    this.save = save;
    this.append = append;
  }

  /**
   * 워크플로우 틀 현재본과 그 템플릿 현재본으로 슬롯을 찍어 실행을 시작함. 틀이나 템플릿이 없으면 거부함.
   *
   * @param templateId 워크플로우 틀 식별자
   * @return 시작한 실행
   */
  public Workflow start(WorkflowTemplateId templateId) {
    WorkflowTemplate template =
        findTemplate
            .apply(templateId)
            .orElseThrow(
                () -> new NoSuchElementException("unknown workflow template id: " + templateId));
    List<StepTemplate> steps =
        template.steps().stream()
            .map(
                stepId ->
                    findStepTemplate
                        .apply(stepId)
                        .orElseThrow(
                            () -> new NoSuchElementException("unknown template id: " + stepId)))
            .toList();
    Workflow started = Workflow.start(template, slotFactory.create(steps));
    store(Map.of(), started);
    return started;
  }

  /**
   * 식별자로 실행을 찾음.
   *
   * @param id 워크플로우 식별자
   * @return 실행. 없으면 빈 값
   */
  public Optional<Workflow> findById(WorkflowId id) {
    return findById.apply(id);
  }

  /**
   * 사가의 현재 노드가 낸 출력으로 실행을 진행함. 실행이 없으면 거부함.
   *
   * @param id 워크플로우 식별자
   * @param sagaId 출력을 낸 사가
   * @param output 현재 노드의 출력
   * @return 진행한 실행
   */
  public Workflow advance(WorkflowId id, SagaId sagaId, StepOutput output) {
    Workflow current =
        findById(id).orElseThrow(() -> new NoSuchElementException("unknown workflow id: " + id));
    Workflow advanced = current.advance(sagaId, output);
    store(current.sagas(), advanced);
    return advanced;
  }

  private void store(Map<SagaId, Saga> before, Workflow workflow) {
    save.accept(workflow);
    LocalDateTime now = LocalDateTime.now();
    for (Saga saga : workflow.sagas().values()) {
      if (!saga.equals(before.get(saga.id()))) {
        append.accept(new SagaLog(UuidV7.generate(), saga, now));
      }
    }
  }
}
