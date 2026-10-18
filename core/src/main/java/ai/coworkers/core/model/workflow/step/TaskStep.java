package ai.coworkers.core.model.workflow.step;

import static ai.coworkers.core.policy.argument.ArgumentSpecifications.NOT_BLANK;

import ai.coworkers.core.policy.Specification;
import java.util.Map;

/**
 * 작업 종류에 맞는 작업을 실행하는 단계
 *
 * @param key        단계 키
 * @param taskType   작업 종류
 * @param parameters 작업 파라미터
 */
public record TaskStep(
        String key,
        TaskType taskType,
        Map<String, String> parameters
) implements StepDefinition {

    private static final Specification<String> KEY =
            NOT_BLANK.orElseThrow(value -> new IllegalArgumentException("key must not be blank"));

    public TaskStep {
        KEY.isSatisfiedBy(key);

        parameters = Map.copyOf(parameters);
    }
}
