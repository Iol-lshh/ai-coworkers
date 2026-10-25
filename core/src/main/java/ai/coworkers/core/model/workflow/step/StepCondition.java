package ai.coworkers.core.model.workflow.step;

import ai.coworkers.core.common.Specification;
import ai.coworkers.core.model.workflow.step.input.StepInput;
import java.util.UUID;

public interface StepCondition extends Specification<StepInput> {
  UUID id();
}
