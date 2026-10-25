package ai.coworkers.core.model.runtime.step.node;

import ai.coworkers.core.model.runtime.step.output.StepOutput;
import ai.coworkers.core.model.runtime.step.slot.StepSlot;
import ai.coworkers.core.model.template.step.input.StepInput;

public interface StepNode {
  StepNodeId id();

  StepSlot stepSlot();

  StepNodeState nodeState();

  default StepOutput run(StepInput input) {
    return stepSlot().process(input);
  }
}
