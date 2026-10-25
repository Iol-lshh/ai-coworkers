package ai.coworkers.core.model.workflow.saga;

import ai.coworkers.core.model.workflow.step.node.StepNode;

public record Saga(SagaId id, StepNode activeNode) {}
