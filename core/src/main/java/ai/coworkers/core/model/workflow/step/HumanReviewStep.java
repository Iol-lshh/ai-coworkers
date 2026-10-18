package ai.coworkers.core.model.workflow.step;

import static ai.coworkers.core.policy.argument.ArgumentSpecifications.NOT_BLANK;

import ai.coworkers.core.policy.Specification;

/**
 * 대상 단계의 산출물을 사람이 리뷰할 때까지 기다리는 단계
 *
 * @param key           단계 키
 * @param targetStepKey 리뷰 대상 산출물을 만든 단계 키
 */
public record HumanReviewStep(
        String key,
        String targetStepKey
) implements StepDefinition {

    private static final Specification<String> KEY =
            NOT_BLANK.orElseThrow(value -> new IllegalArgumentException("key must not be blank"));
    private static final Specification<String> TARGET_STEP_KEY =
            NOT_BLANK.orElseThrow(value -> new IllegalArgumentException("targetStepKey must not be blank"));

    public HumanReviewStep {
        KEY.isSatisfiedBy(key);
        TARGET_STEP_KEY.isSatisfiedBy(targetStepKey);
    }
}
