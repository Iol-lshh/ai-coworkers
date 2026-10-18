package ai.coworkers.core.model.workflow.step;

import static ai.coworkers.core.policy.argument.ArgumentSpecifications.NOT_BLANK;
import static ai.coworkers.core.policy.argument.ArgumentSpecifications.NOT_EMPTY;

import ai.coworkers.core.policy.Specification;

import java.util.Collection;
import java.util.List;

/**
 * 조건을 만족하면 참 단계를, 만족하지 않으면 거짓 단계를 실행하는 단계
 *
 * @param key           단계 키
 * @param conditionCode 분기 조건 식별 코드
 * @param thenSteps     조건을 만족할 때 실행할 하위 단계
 * @param elseSteps     조건을 만족하지 않을 때 실행할 하위 단계. 비어 있으면 아무것도 실행하지 않음
 */
public record BranchStep(
        String key,
        String conditionCode,
        List<StepDefinition> thenSteps,
        List<StepDefinition> elseSteps
) implements StepDefinition {

    private static final Specification<String> KEY =
            NOT_BLANK.orElseThrow(value -> new IllegalArgumentException("key must not be blank"));
    private static final Specification<String> CONDITION_CODE =
            NOT_BLANK.orElseThrow(value -> new IllegalArgumentException("conditionCode must not be blank"));
    private static final Specification<Collection<?>> THEN_STEPS =
            NOT_EMPTY.orElseThrow(value -> new IllegalArgumentException("thenSteps must not be empty"));

    public BranchStep {
        KEY.isSatisfiedBy(key);
        CONDITION_CODE.isSatisfiedBy(conditionCode);
        THEN_STEPS.isSatisfiedBy(thenSteps);
        thenSteps = List.copyOf(thenSteps);
        elseSteps = List.copyOf(elseSteps);
    }
}
