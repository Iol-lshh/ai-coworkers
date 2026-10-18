package ai.coworkers.core.model.workflow;

import static ai.coworkers.core.policy.argument.ArgumentSpecifications.NOT_BLANK;
import static ai.coworkers.core.policy.argument.ArgumentSpecifications.NOT_EMPTY;
import static ai.coworkers.core.policy.argument.ArgumentSpecifications.POSITIVE;

import ai.coworkers.core.model.workflow.step.BranchStep;
import ai.coworkers.core.model.workflow.step.ForEachStep;
import ai.coworkers.core.model.workflow.step.HumanReviewStep;
import ai.coworkers.core.model.workflow.step.LoopUntilStep;
import ai.coworkers.core.model.workflow.step.StepDefinition;
import ai.coworkers.core.model.workflow.step.TaskStep;
import ai.coworkers.core.policy.Specification;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 버전이 고정된 워크플로우 정의
 *
 * @param key     워크플로우 키
 * @param version 정의 버전
 * @param steps   최상위 단계
 */
public record WorkflowDefinition(
        String key,
        int version,
        List<StepDefinition> steps
) {

    private static final Specification<String> KEY =
            NOT_BLANK.orElseThrow(value -> new IllegalArgumentException("key must not be blank"));
    private static final Specification<Integer> VERSION =
            POSITIVE.orElseThrow(value -> new IllegalArgumentException("version must be positive"));
    private static final Specification<Collection<?>> STEPS =
            NOT_EMPTY.orElseThrow(value -> new IllegalArgumentException("steps must not be empty"));

    public WorkflowDefinition {
        KEY.isSatisfiedBy(key);
        VERSION.isSatisfiedBy(version);
        STEPS.isSatisfiedBy(steps);
        steps = List.copyOf(steps);
        validateStepKeys(steps);
    }

    /**
     * 하위 단계를 포함한 모든 단계를 정의 순서대로 반환함.
     *
     * @return 모든 단계
     */
    public List<StepDefinition> allSteps() {
        return flatten(steps).toList();
    }

    private static void validateStepKeys(List<StepDefinition> steps) {
        Set<String> keys = new HashSet<>();
        flatten(steps).forEach(step -> {
            if (!keys.add(step.key())) {
                throw new IllegalArgumentException("duplicate step key: " + step.key());
            }
        });
        flatten(steps).forEach(step -> {
            for (String reference : references(step)) {
                if (!keys.contains(reference)) {
                    throw new IllegalArgumentException("unknown step key: " + reference);
                }
            }
        });
    }

    private static Stream<StepDefinition> flatten(List<StepDefinition> steps) {
        return steps.stream().flatMap(step -> Stream.concat(Stream.of(step), flatten(children(step))));
    }

    private static List<StepDefinition> children(StepDefinition step) {
        return switch (step) {
            case TaskStep ignored -> List.of();
            case HumanReviewStep ignored -> List.of();
            case BranchStep branch -> Stream.concat(branch.thenSteps().stream(), branch.elseSteps().stream()).toList();
            case LoopUntilStep loop -> loop.body();
            case ForEachStep forEach -> forEach.body();
        };
    }

    private static List<String> references(StepDefinition step) {
        return switch (step) {
            case TaskStep ignored -> List.of();
            case HumanReviewStep review -> List.of(review.targetStepKey());
            case BranchStep ignored -> List.of();
            case LoopUntilStep ignored -> List.of();
            case ForEachStep forEach -> List.of(forEach.sourceStepKey());
        };
    }
}
