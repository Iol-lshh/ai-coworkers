package ai.coworkers.core.model.template.workflow.log;

/** 워크플로우 틀 로그 저장소. 추가만 하고 수정하지 않음. */
public interface WorkflowTemplateLogRepository {

  /**
   * 로그를 추가함.
   *
   * @param log 추가할 로그
   */
  void append(WorkflowTemplateLog log);
}
