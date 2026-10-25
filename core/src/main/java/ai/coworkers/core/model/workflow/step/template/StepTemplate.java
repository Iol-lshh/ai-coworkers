package ai.coworkers.core.model.workflow.step.template;

import ai.coworkers.core.model.workflow.step.StepType;

/** 슬롯으로 찍어낼 단계의 틀 */
public interface StepTemplate {
  StepTemplateId id();

  StepType stepType();

  boolean isTaskStep();

  Long version();
}
