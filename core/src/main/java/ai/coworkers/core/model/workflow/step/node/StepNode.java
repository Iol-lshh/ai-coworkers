package ai.coworkers.core.model.workflow.step.node;

import ai.coworkers.core.model.workflow.step.input.StepInput;
import ai.coworkers.core.model.workflow.step.output.StepOutput;
import ai.coworkers.core.model.workflow.step.slot.StepSlot;

public interface StepNode {
  StepNodeId id();

  StepSlot stepSlot();

  StepNodeState nodeState();

  default StepOutput run(StepInput input) {
    return stepSlot().process(input);
  }
}
