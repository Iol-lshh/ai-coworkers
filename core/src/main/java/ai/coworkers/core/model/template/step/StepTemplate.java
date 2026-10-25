package ai.coworkers.core.model.template.step;

/** 슬롯으로 찍어낼 단계의 틀 */
public interface StepTemplate {
  StepTemplateId id();

  StepType stepType();

  boolean isTaskStep();

  Long version();
}
