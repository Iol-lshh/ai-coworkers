package ai.coworkers.core.model.workflow.step.template;

/** 템플릿 로그 저장소. 추가만 하고 수정하지 않음. */
public interface StepTemplateLogRepository {

  /**
   * 로그를 추가함.
   *
   * @param log 추가할 로그
   */
  void append(StepTemplateLog log);
}
