package ai.coworkers.core.model.workflow.step.template;

import ai.coworkers.core.model.workflow.step.condition.StepConditionCodec;
import ai.coworkers.core.model.workflow.step.template.StepTemplateData.BranchData;
import ai.coworkers.core.model.workflow.step.template.StepTemplateData.LoopData;
import ai.coworkers.core.model.workflow.step.template.StepTemplateData.ParallelData;
import java.util.List;
import java.util.UUID;

/** 템플릿을 저장 형태로 바꾸고, 저장 형태로 템플릿을 만드는 진입점. 작업 템플릿은 저장 형태가 없어 거부함. */
public final class StepTemplateCodec {

  private final StepConditionCodec conditionCodec;

  /**
   * 조건 트리 변환에 쓸 코덱을 받음.
   *
   * @param conditionCodec 조건 트리 코덱
   */
  public StepTemplateCodec(StepConditionCodec conditionCodec) {
    this.conditionCodec = conditionCodec;
  }

  /**
   * 템플릿을 저장 형태로 바꿈. 저장 형태가 없는 템플릿 종류면 거부함.
   *
   * @param template 템플릿
   * @return 저장 형태
   */
  public StepTemplateData encode(StepTemplate template) {
    return switch (template) {
      case BranchStepTemplate branch ->
          new BranchData(
              branch.id().value(),
              branch.version(),
              conditionCodec.encode(branch.condition()),
              branch.whenSatisfied().value(),
              branch.otherwise().value());
      case LoopStepTemplate loop ->
          new LoopData(
              loop.id().value(),
              loop.version(),
              conditionCodec.encode(loop.condition()),
              loop.body().value(),
              loop.exit().value());
      case ParallelStepTemplate parallel ->
          new ParallelData(
              parallel.id().value(),
              parallel.version(),
              parallel.branches().stream().map(StepTemplateId::value).toList(),
              parallel.join().value());
      default ->
          throw new IllegalArgumentException(
              "unsupported template type: " + template.getClass().getName());
    };
  }

  /**
   * 저장 형태로 템플릿을 만듦. 조건 트리 복원 규칙을 어기면 거부함.
   *
   * @param data 저장 형태
   * @return 템플릿
   */
  public StepTemplate decode(StepTemplateData data) {
    return switch (data) {
      case BranchData branch ->
          new BranchStepTemplate(
              new StepTemplateId(branch.id()),
              branch.version(),
              conditionCodec.decode(branch.condition()),
              new StepTemplateId(branch.whenSatisfied()),
              new StepTemplateId(branch.otherwise()));
      case LoopData loop ->
          new LoopStepTemplate(
              new StepTemplateId(loop.id()),
              loop.version(),
              conditionCodec.decode(loop.condition()),
              new StepTemplateId(loop.body()),
              new StepTemplateId(loop.exit()));
      case ParallelData parallel ->
          new ParallelStepTemplate(
              new StepTemplateId(parallel.id()),
              parallel.version(),
              ids(parallel.branches()),
              new StepTemplateId(parallel.join()));
    };
  }

  private static List<StepTemplateId> ids(List<UUID> values) {
    return values.stream().map(StepTemplateId::new).toList();
  }
}
