package ai.coworkers.core.model.workflow.step;

public enum DefaultStepType implements StepType {
  TASK,
  BRANCH,
  LOOP,
  PARALLEL
}
