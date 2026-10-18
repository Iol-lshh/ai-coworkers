package ai.coworkers.core.model.workflow.step;

import static ai.coworkers.core.policy.argument.ArgumentSpecifications.NOT_BLANK;
import static ai.coworkers.core.policy.argument.ArgumentSpecifications.NOT_EMPTY;
import static ai.coworkers.core.policy.argument.ArgumentSpecifications.POSITIVE;

import ai.coworkers.core.policy.Specification;

import java.util.Collection;
import java.util.List;

/**
 * 종료 조건을 만족하거나 최대 횟수에 이를 때까지 하위 단계를 반복하는 단계
 *
 * @param key           단계 키
 * @param conditionCode 종료 조건 식별 코드
 * @param maxIterations 최대 반복 횟수
 * @param body          반복할 하위 단계
 */
public record LoopUntilStep(
        String key,
        String conditionCode,
        int maxIterations,
        List<StepDefinition> body
) implements StepDefinition {

    private static final Specification<String> KEY =
            NOT_BLANK.orElseThrow(value -> new IllegalArgumentException("key must not be blank"));
    private static final Specification<String> CONDITION_CODE =
            NOT_BLANK.orElseThrow(value -> new IllegalArgumentException("conditionCode must not be blank"));
    private static final Specification<Integer> MAX_ITERATIONS =
            POSITIVE.orElseThrow(value -> new IllegalArgumentException("maxIterations must be positive"));
    private static final Specification<Collection<?>> BODY =
            NOT_EMPTY.orElseThrow(value -> new IllegalArgumentException("body must not be empty"));

    public LoopUntilStep {
        KEY.isSatisfiedBy(key);
        CONDITION_CODE.isSatisfiedBy(conditionCode);
        MAX_ITERATIONS.isSatisfiedBy(maxIterations);
        BODY.isSatisfiedBy(body);
        body = List.copyOf(body);
    }
}
