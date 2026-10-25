package ai.coworkers.core.model.template.workflow;

import ai.coworkers.core.model.template.step.StepTemplateId;

/** 워크플로우 틀을 저장 형태로 바꾸고, 저장 형태로 틀을 만드는 진입점 */
public final class WorkflowTemplateCodec {

  /**
   * 틀을 저장 형태로 바꿈.
   *
   * @param template 워크플로우 틀
   * @return 저장 형태
   */
  public WorkflowTemplateData encode(WorkflowTemplate template) {
    return new WorkflowTemplateData(
        template.id().value(),
        template.version(),
        template.start().value(),
        template.steps().stream().map(StepTemplateId::value).toList());
  }

  /**
   * 저장 형태로 틀을 만듦. 틀 규칙을 어기면 거부함.
   *
   * @param data 저장 형태
   * @return 워크플로우 틀
   */
  public WorkflowTemplate decode(WorkflowTemplateData data) {
    return new WorkflowTemplate(
        new WorkflowTemplateId(data.id()),
        data.version(),
        new StepTemplateId(data.start()),
        data.steps().stream().map(StepTemplateId::new).toList());
  }
}
