package ai.coworkers.core.model.workflow.step;

import static ai.coworkers.core.policy.argument.ArgumentSpecifications.NOT_BLANK;
import static ai.coworkers.core.policy.argument.ArgumentSpecifications.NOT_EMPTY;

import ai.coworkers.core.policy.Specification;
import java.util.Collection;
import java.util.List;

/**
 * 원천 단계가 만든 목록의 항목마다 하위 단계를 실행하는 단계
 *
 * @param key           단계 키
 * @param sourceStepKey 순회할 목록을 만든 단계 키
 * @param body          항목마다 실행할 하위 단계
 */
public record ForEachStep(
        String key,
        String sourceStepKey,
        List<StepDefinition> body
) implements StepDefinition {

    private static final Specification<String> KEY =
            NOT_BLANK.orElseThrow(value -> new IllegalArgumentException("key must not be blank"));
    private static final Specification<String> SOURCE_STEP_KEY =
            NOT_BLANK.orElseThrow(value -> new IllegalArgumentException("sourceStepKey must not be blank"));
    private static final Specification<Collection<?>> BODY =
            NOT_EMPTY.orElseThrow(value -> new IllegalArgumentException("body must not be empty"));

    public ForEachStep {
        KEY.isSatisfiedBy(key);
        SOURCE_STEP_KEY.isSatisfiedBy(sourceStepKey);
        BODY.isSatisfiedBy(body);
        body = List.copyOf(body);
    }
}
